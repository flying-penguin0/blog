package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Announcement;
import com.blog.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 * 
 * @author Blog System
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 获取所有公告列表（管理员）
     */
    @Operation(summary = "获取所有公告列表")
    @GetMapping("/list")
    public Result<List<Announcement>> getAnnouncementList(
            @RequestParam(required = false) String title,
            HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("无权限访问");
        }
        List<Announcement> announcements = announcementService.getAnnouncementList(title);
        return Result.success(announcements);
    }
    
    /**
     * 获取最新公告（前台展示）
     */
    @Operation(summary = "获取最新公告")
    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return Result.success(announcements);
    }
    
    /**
     * 根据ID获取公告详情
     */
    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        return Result.success(announcement);
    }
    
    /**
     * 创建公告（仅管理员）
     */
    @Operation(summary = "创建公告")
    @PostMapping
    public Result<Void> createAnnouncement(@RequestBody Announcement announcement, 
                                          HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("无权限操作");
        }
        announcementService.createAnnouncement(announcement);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新公告（仅管理员）
     */
    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id,
                                          @RequestBody Announcement announcement,
                                          HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("无权限操作");
        }
        announcement.setId(id);
        announcementService.updateAnnouncement(announcement);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除公告（仅管理员）
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id, HttpServletRequest request) {
        // 验证管理员权限
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("无权限操作");
        }
        announcementService.deleteAnnouncement(id);
        return Result.success("删除成功", null);
    }
}
