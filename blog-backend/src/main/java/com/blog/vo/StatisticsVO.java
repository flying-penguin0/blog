package com.blog.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统计数据 VO
 * 
 * @author Blog System
 */
@Data
public class StatisticsVO {
    
    /**
     * 用户总数
     */
    private Long userCount;
    
    /**
     * 文章总数
     */
    private Long articleCount;
    
    /**
     * 评论总数
     */
    private Long commentCount;
    
    /**
     * 总浏览量
     */
    private Long totalViews;
    
    /**
     * 分类统计数据
     */
    private List<Map<String, Object>> categoryStats;
    
    /**
     * 标签统计数据
     */
    private List<Map<String, Object>> tagStats;
}
