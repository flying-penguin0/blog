package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Announcement;
import com.blog.exception.BusinessException;
import com.blog.mapper.AnnouncementMapper;
import com.blog.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务实现类
 * 
 * @author Blog System
 */
@Slf4j
@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    
    @Autowired
    private AnnouncementMapper announcementMapper;
    
    @Override
    public List<Announcement> getAnnouncementList(String title) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 如果提供了标题参数，添加模糊查询条件
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Announcement::getTitle, title.trim());
        }
        
        wrapper.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }
    
    @Override
    public List<Announcement> getLatestAnnouncements(int limit) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return announcementMapper.selectList(wrapper);
    }
    
    @Override
    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }
    
    @Override
    public void createAnnouncement(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
            throw new BusinessException("公告内容不能为空");
        }
        announcementMapper.insert(announcement);
    }
    
    @Override
    public void updateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Announcement existingAnnouncement = announcementMapper.selectById(announcement.getId());
        if (existingAnnouncement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
            throw new BusinessException("公告内容不能为空");
        }
        announcementMapper.updateById(announcement);
    }
    
    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }
}
