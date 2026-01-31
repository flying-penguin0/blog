package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.entity.ArticleTag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.StatisticsService;
import com.blog.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 * 
 * @author Blog System
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private ArticleTagMapper articleTagMapper;
    
    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();
        
        // 统计用户总数
        vo.setUserCount(userMapper.selectCount(null));
        
        // 统计文章总数
        vo.setArticleCount(articleMapper.selectCount(null));
        
        // 统计评论总数（只统计已审核通过的）
        LambdaQueryWrapper<com.blog.entity.Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(com.blog.entity.Comment::getStatus, "approved");
        vo.setCommentCount(commentMapper.selectCount(commentWrapper));
        
        // 统计总浏览量
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        List<Article> articles = articleMapper.selectList(articleWrapper);
        Long totalViews = articles.stream()
                .mapToLong(article -> article.getViewCount() != null ? article.getViewCount() : 0L)
                .sum();
        vo.setTotalViews(totalViews);
        
        // 统计分类数据
        List<Category> categories = categoryMapper.selectList(null);
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (Category category : categories) {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getCategoryId, category.getId());
            long count = articleMapper.selectCount(wrapper);
            if (count > 0) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", category.getName());
                stat.put("value", count);
                categoryStats.add(stat);
            }
        }
        vo.setCategoryStats(categoryStats);
        
        // 统计标签数据
        List<Tag> tags = tagMapper.selectList(null);
        List<Map<String, Object>> tagStats = new ArrayList<>();
        for (Tag tag : tags) {
            LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArticleTag::getTagId, tag.getId());
            long count = articleTagMapper.selectCount(wrapper);
            if (count > 0) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", tag.getName());
                stat.put("value", count);
                tagStats.add(stat);
            }
        }
        // 按文章数量降序排序，取前10个
        tagStats.sort((a, b) -> Long.compare((Long)b.get("value"), (Long)a.get("value")));
        if (tagStats.size() > 10) {
            tagStats = tagStats.subList(0, 10);
        }
        vo.setTagStats(tagStats);
        
        return vo;
    }
}
