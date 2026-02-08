package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.ResultCode;
import com.blog.dto.ArticleDTO;
import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import com.blog.utils.RedisUtil;
import com.blog.vo.ArticleVO;
import com.blog.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 * 
 * @author Blog System
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private ArticleTagMapper articleTagMapper;
    
    @Autowired
    private com.blog.mapper.CommentMapper commentMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(ArticleDTO dto, Long userId) {
        // 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setCoverImage(dto.getCoverImage());
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : "published");
        article.setViewCount(0);
        
        if ("published".equals(article.getStatus())) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        this.save(article);
        
        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTagIds());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleDTO dto, Long userId, String role) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 如果 role 为 null，从数据库查询用户角色（兼容旧 Token）
        if (role == null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                role = user.getRole();
            }
        }
        
        // 打印调试信息
        System.out.println("=== 文章更新权限检查 ===");
        System.out.println("文章ID: " + id);
        System.out.println("文章作者ID: " + article.getUserId());
        System.out.println("当前用户ID: " + userId);
        System.out.println("当前用户角色: " + role);
        System.out.println("是否是管理员: " + "admin".equals(role));
        System.out.println("是否是文章作者: " + article.getUserId().equals(userId));
        
        // 检查权限：管理员可以编辑所有文章，普通用户只能编辑自己的文章
        if (!"admin".equals(role) && !article.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 更新文章
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setCoverImage(dto.getCoverImage());
        
        // 更新状态，如果从私密变为公开，设置发布时间
        String oldStatus = article.getStatus();
        String newStatus = dto.getStatus();
        if ("private".equals(oldStatus) && "published".equals(newStatus)) {
            article.setPublishTime(LocalDateTime.now());
        }
        article.setStatus(newStatus);
        
        this.updateById(article);
        
        // 更新标签关联
        if (dto.getTagIds() != null) {
            // 删除旧的标签关联
            LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArticleTag::getArticleId, id);
            articleTagMapper.delete(wrapper);
            
            // 保存新的标签关联
            if (!dto.getTagIds().isEmpty()) {
                saveArticleTags(id, dto.getTagIds());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id, Long userId, String role) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 如果 role 为 null，从数据库查询用户角色（兼容旧 Token）
        if (role == null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                role = user.getRole();
            }
        }
        
        // 检查权限：管理员可以删除所有文章，普通用户只能删除自己的文章
        if (!"admin".equals(role) && !article.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 删除文章
        this.removeById(id);
        
        // 删除标签关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(wrapper);
    }
    
    @Override
    public ArticleVO getArticleDetail(Long id) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        return buildArticleVO(article);
    }
    
    @Override
    public Page<ArticleVO> getArticleList(Integer page, Integer size, Long categoryId, Long tagId, String keyword, Long userId, String status) {
        Page<Article> articlePage = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        // 用户ID筛选（用于用户后台查看自己的文章）
        if (userId != null) {
            wrapper.eq(Article::getUserId, userId);
        }
        // 注意：管理员查看所有文章时，userId 为 null，不添加任何用户ID和状态限制
        
        // 状态筛选（用于筛选公开/私密）
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Article::getStatus, status);
        }
        
        // 分类筛选（通过标签的分类来筛选文章）
        if (categoryId != null) {
            // 查询该分类下的所有标签
            LambdaQueryWrapper<Tag> tagCategoryWrapper = new LambdaQueryWrapper<>();
            tagCategoryWrapper.eq(Tag::getCategoryId, categoryId);
            List<Tag> tags = tagMapper.selectList(tagCategoryWrapper);
            
            if (!tags.isEmpty()) {
                List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
                
                // 查询包含这些标签的文章
                LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
                articleTagWrapper.in(ArticleTag::getTagId, tagIds);
                List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagWrapper);
                
                List<Long> articleIds = articleTags.stream()
                        .map(ArticleTag::getArticleId)
                        .distinct()
                        .collect(Collectors.toList());
                
                if (!articleIds.isEmpty()) {
                    wrapper.in(Article::getId, articleIds);
                } else {
                    // 如果该分类下没有文章，返回空结果
                    return new Page<>(page, size);
                }
            } else {
                // 如果该分类下没有标签，返回空结果
                return new Page<>(page, size);
            }
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getContent, keyword));
        }
        
        // 标签筛选
        if (tagId != null) {
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getTagId, tagId);
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toList());
            if (!articleIds.isEmpty()) {
                wrapper.in(Article::getId, articleIds);
            } else {
                // 如果没有文章，返回空结果
                return new Page<>(page, size);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> result = this.page(articlePage, wrapper);
        
        // 转换为 VO
        Page<ArticleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ArticleVO> voList = result.getRecords().stream()
                .map(this::buildArticleVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public List<ArticleVO> getHotArticles(Integer limit) {
        String redisKey = "article:hot:ranking";
        
        // 从 Redis 获取热门文章排名（按阅读量降序）
        Set<Object> hotArticleIds = redisUtil.zReverseRange(redisKey, 0, limit - 1);
        
        if (hotArticleIds != null && !hotArticleIds.isEmpty()) {
            // 从数据库批量查询文章详情
            List<Long> ids = hotArticleIds.stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(Collectors.toList());
            
            List<Article> articles = this.listByIds(ids);
            
            // 按照 Redis 中的排序返回
            Map<Long, Article> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, a -> a));
            
            List<ArticleVO> result = new ArrayList<>();
            int rank = 1;
            for (Object id : hotArticleIds) {
                Long articleId = Long.parseLong(id.toString());
                Article article = articleMap.get(articleId);
                if (article != null && "published".equals(article.getStatus())) {
                    ArticleVO vo = buildArticleVO(article);
                    vo.setRank(rank++); // 设置排名
                    result.add(vo);
                }
            }
            return result;
        }
        
        // Redis 中没有数据，从数据库查询并更新到 Redis
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, "published");
        wrapper.orderByDesc(Article::getViewCount);
        wrapper.last("LIMIT " + limit);
        
        List<Article> articles = this.list(wrapper);
        
        // 更新到 Redis
        for (Article article : articles) {
            redisUtil.zAdd(redisKey, article.getId().toString(), article.getViewCount());
        }
        
        // 设置过期时间为 1 小时
        redisUtil.expire(redisKey, 3600);
        
        List<ArticleVO> result = new ArrayList<>();
        int rank = 1;
        for (Article article : articles) {
            ArticleVO vo = buildArticleVO(article);
            vo.setRank(rank++);
            result.add(vo);
        }
        return result;
    }
    
    @Override
    public void incrementViewCount(Long id) {
        Article article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            this.updateById(article);
            
            // 更新 Redis 中的浏览量
            redisUtil.increment("article:view:" + id);
            
            // 更新热门文章排名
            String rankingKey = "article:hot:ranking";
            redisUtil.zAdd(rankingKey, id.toString(), article.getViewCount());
        }
    }
    
    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }
    
    /**
     * 构建文章 VO
     */
    private ArticleVO buildArticleVO(Article article) {
        ArticleVO vo = BeanUtil.copyProperties(article, ArticleVO.class);
        
        // 查询作者信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            vo.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAuthorAvatar(user.getAvatar());
        }
        
        // 查询标签列表
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, article.getId());
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);
        
        List<TagVO> tagVOList = new ArrayList<>();
        Long categoryId = null;
        String categoryName = null;
        
        for (ArticleTag articleTag : articleTags) {
            Tag tag = tagMapper.selectById(articleTag.getTagId());
            if (tag != null) {
                TagVO tagVO = BeanUtil.copyProperties(tag, TagVO.class);
                tagVOList.add(tagVO);
                
                // 获取第一个标签的分类信息作为文章的分类
                if (categoryId == null && tag.getCategoryId() != null) {
                    categoryId = tag.getCategoryId();
                    Category category = categoryMapper.selectById(tag.getCategoryId());
                    if (category != null) {
                        categoryName = category.getName();
                    }
                }
            }
        }
        vo.setTags(tagVOList);
        vo.setCategoryId(categoryId);
        vo.setCategoryName(categoryName);
        
        // 查询评论数量（通过关联查询comment表）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getArticleId, article.getId());
        Long commentCount = commentMapper.selectCount(commentWrapper);
        vo.setCommentCount(commentCount.intValue());
        
        return vo;
    }
}
