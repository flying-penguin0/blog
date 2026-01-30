package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务接口
 * 
 * @author Blog System
 */
public interface ArticleService extends IService<Article> {
    
    /**
     * 创建文章
     */
    void createArticle(ArticleDTO dto, Long userId);
    
    /**
     * 更新文章
     */
    void updateArticle(Long id, ArticleDTO dto, Long userId, String role);
    
    /**
     * 删除文章
     */
    void deleteArticle(Long id, Long userId, String role);
    
    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long id);
    
    /**
     * 分页查询文章列表
     */
    Page<ArticleVO> getArticleList(Integer page, Integer size, Long categoryId, Long tagId, String keyword, Long userId, String status);
    
    /**
     * 获取热门文章
     */
    List<ArticleVO> getHotArticles(Integer limit);
    
    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);
}
