package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.StatisticsService;
import com.blog.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计数据控制器
 * 
 * @author Blog System
 */
@Tag(name = "统计数据")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取统计数据
     */
    @Operation(summary = "获取统计数据")
    @GetMapping
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO statistics = statisticsService.getStatistics();
        return Result.success(statistics);
    }
}
