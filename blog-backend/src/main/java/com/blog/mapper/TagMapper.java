package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 标签 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 查询标签的文章数量
     */
    @Select("SELECT COUNT(DISTINCT at.article_id) FROM article_tag at " +
            "INNER JOIN article a ON at.article_id = a.id " +
            "WHERE at.tag_id = #{tagId} AND a.status = 'published'")
    Integer countArticlesByTagId(Long tagId);
}
