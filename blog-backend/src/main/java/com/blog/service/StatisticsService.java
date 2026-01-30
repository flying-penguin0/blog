package com.blog.service;

import com.blog.vo.StatisticsVO;

/**
 * 统计服务接口
 * 
 * @author Blog System
 */
public interface StatisticsService {
    
    /**
     * 获取统计数据
     */
    StatisticsVO getStatistics();
}
