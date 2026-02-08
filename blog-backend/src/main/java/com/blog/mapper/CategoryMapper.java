package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 分类 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 查询分类的文章数量（通过标签关联）
     * 查询逻辑：category -> tag -> article_tag -> article
     */
    @Select("SELECT COUNT(DISTINCT at.article_id) " +
            "FROM tag t " +
            "INNER JOIN article_tag at ON t.id = at.tag_id " +
            "INNER JOIN article a ON at.article_id = a.id " +
            "WHERE t.category_id = #{categoryId} AND a.status = 'published'")
    Integer countArticlesByCategoryId(Long categoryId);
}
