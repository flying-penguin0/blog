package com.blog.service;

import com.blog.entity.Announcement;

import java.util.List;

/**
 * 公告服务接口
 * 
 * @author Blog System
 */
public interface AnnouncementService {
    
    /**
     * 获取所有公告列表
     */
    List<Announcement> getAnnouncementList();
    
    /**
     * 获取最新的公告列表
     */
    List<Announcement> getLatestAnnouncements(int limit);
    
    /**
     * 根据ID获取公告
     */
    Announcement getAnnouncementById(Long id);
    
    /**
     * 创建公告
     */
    void createAnnouncement(Announcement announcement);
    
    /**
     * 更新公告
     */
    void updateAnnouncement(Announcement announcement);
    
    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);
}
