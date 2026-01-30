package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论点赞 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    
    /**
     * 获取评论的点赞数
     */
    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int getCommentLikeCount(@Param("commentId") Long commentId);
    
    /**
     * 批量查询用户对多条评论的点赞状态
     */
    @Select("<script>" +
            "SELECT comment_id FROM comment_like " +
            "WHERE user_id = #{userId} AND comment_id IN " +
            "<foreach collection='commentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> getUserLikedComments(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}
