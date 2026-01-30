package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
