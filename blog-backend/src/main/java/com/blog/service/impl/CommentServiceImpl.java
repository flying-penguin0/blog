package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.dto.CommentDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.CommentLike;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.CommentLikeMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.CommentService;
import com.blog.utils.SensitiveWordUtil;
import com.blog.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * 
 * @author Blog System
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createComment(CommentDTO dto, Long userId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        
        // 如果是二级评论，检查父评论是否存在
        if (dto.getParentId() != null) {
            Comment parentComment = this.getById(dto.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
        }
        
        // 敏感词检测
        String content = dto.getContent();
        boolean hasSensitiveWord = sensitiveWordUtil.containsSensitiveWord(content);
        
        // 创建评论
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setContent(content);
        
        // 根据敏感词检测结果设置状态
        String status;
        if (hasSensitiveWord) {
            status = "pending"; // 包含敏感词，待审核
        } else {
            status = "approved"; // 不包含敏感词，自动通过
        }
        comment.setStatus(status);
        
        this.save(comment);
        
        // 只有通过审核的评论才增加文章评论数
        if ("approved".equals(status)) {
            article.setCommentCount(article.getCommentCount() + 1);
            articleMapper.updateById(article);
        }
        
        return status;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId, String role) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 如果 role 为 null，从数据库查询用户角色（兼容旧 Token）
        if (role == null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                role = user.getRole();
            }
        }
        
        // 检查权限：管理员可以删除所有评论，普通用户只能删除自己的评论
        if (!"admin".equals(role) && !comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 删除评论
        this.removeById(id);
        
        // 如果是一级评论，删除所有子评论
        if (comment.getParentId() == null) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId, id);
            this.remove(wrapper);
        }
        
        // 只有已通过审核的评论被删除时，才减少文章评论数
        if ("approved".equals(comment.getStatus())) {
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
                articleMapper.updateById(article);
            }
        }
    }
    
    @Override
    public List<CommentVO> getArticleComments(Long articleId, Long currentUserId) {
        // 查询所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        wrapper.eq(Comment::getStatus, "approved"); // 只显示已审核通过的评论
        wrapper.orderByDesc(Comment::getCreateTime);
        List<Comment> comments = this.list(wrapper);
        
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有评论ID
        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 批量查询点赞数
        Map<Long, Integer> likeCountMap = commentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> commentLikeMapper.getCommentLikeCount(id)
                ));
        
        // 如果用户已登录，批量查询用户点赞状态
        List<Long> userLikedCommentIds = new ArrayList<>();
        if (currentUserId != null) {
            userLikedCommentIds = commentLikeMapper.getUserLikedComments(currentUserId, commentIds);
        }
        
        // 转换为 VO 并填充用户信息、点赞数和点赞状态
        final List<Long> likedIds = userLikedCommentIds;
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = buildCommentVO(comment);
                    vo.setLikeCount(likeCountMap.getOrDefault(comment.getId(), 0));
                    vo.setIsLiked(likedIds.contains(comment.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildCommentTree(commentVOList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long id, String status) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        String oldStatus = comment.getStatus();
        comment.setStatus(status);
        this.updateById(comment);
        
        // 如果从待审核变为通过，增加文章评论数
        if ("pending".equals(oldStatus) && "approved".equals(status)) {
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                article.setCommentCount(article.getCommentCount() + 1);
                articleMapper.updateById(article);
            }
        }
        // 如果从通过变为拒绝，减少文章评论数
        else if ("approved".equals(oldStatus) && "rejected".equals(status)) {
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
                articleMapper.updateById(article);
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long commentId, Long userId) {
        // 检查评论是否存在
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 查询用户是否已点赞
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId);
        wrapper.eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(wrapper);
        
        if (existingLike != null) {
            // 已点赞，取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
        } else {
            // 未点赞，添加点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreateTime(java.time.LocalDateTime.now());
            commentLikeMapper.insert(like);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditComments(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        
        // 批量更新评论状态
        List<Comment> comments = this.listByIds(ids);
        comments.forEach(comment -> comment.setStatus(status));
        this.updateBatchById(comments);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        
        // 批量删除评论
        this.removeByIds(ids);
        
        // 删除这些评论的所有子评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Comment::getParentId, ids);
        this.remove(wrapper);
    }
    
    @Override
    public PageResult<CommentVO> getAllComments(String keyword, String status, Integer page, Integer size, Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        
        // 用户ID筛选（用于用户后台查看自己发表的评论）
        if (userId != null) {
            // 直接查询该用户发表的评论
            wrapper.eq(Comment::getUserId, userId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }
        
        // 状态筛选（如果指定了状态）
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Comment::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Comment::getCreateTime);
        
        // 查询总数
        Long total = this.count(wrapper);
        
        // 分页查询
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        
        List<Comment> comments = this.list(wrapper);
        
        // 转换为 VO 并填充用户信息和文章信息
        List<CommentVO> commentVOList = comments.stream()
                .map(this::buildCommentVOWithArticle)
                .collect(Collectors.toList());
        
        return new PageResult<>(commentVOList, total, page, size);
    }
    
    /**
     * 构建评论 VO（包含文章信息）
     */
    private CommentVO buildCommentVOWithArticle(Comment comment) {
        CommentVO vo = buildCommentVO(comment);
        
        // 查询文章信息
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            vo.setArticleTitle(article.getTitle());
        }
        
        return vo;
    }
    
    /**
     * 构建评论 VO
     */
    private CommentVO buildCommentVO(Comment comment) {
        CommentVO vo = BeanUtil.copyProperties(comment, CommentVO.class);
        
        // 查询评论用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        
        // 如果是二级评论，查询父评论用户信息
        if (comment.getParentId() != null) {
            Comment parentComment = this.getById(comment.getParentId());
            if (parentComment != null) {
                User parentUser = userMapper.selectById(parentComment.getUserId());
                if (parentUser != null) {
                    vo.setParentUsername(parentUser.getNickname() != null ? 
                            parentUser.getNickname() : parentUser.getUsername());
                }
            }
        }
        
        return vo;
    }
    
    /**
     * 构建评论树形结构
     */
    private List<CommentVO> buildCommentTree(List<CommentVO> comments) {
        // 分离一级评论和二级评论
        List<CommentVO> rootComments = new ArrayList<>();
        Map<Long, List<CommentVO>> replyMap = comments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));
        
        // 构建树形结构
        for (CommentVO comment : comments) {
            if (comment.getParentId() == null) {
                // 一级评论
                comment.setReplies(replyMap.getOrDefault(comment.getId(), new ArrayList<>()));
                rootComments.add(comment);
            }
        }
        
        return rootComments;
    }
}
