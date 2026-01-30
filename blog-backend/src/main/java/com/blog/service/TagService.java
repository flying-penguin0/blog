package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.entity.Tag;
import com.blog.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 * 
 * @author Blog System
 */
public interface TagService extends IService<Tag> {
    
    /**
     * 获取所有标签
     */
    List<TagVO> getAllTags(Long categoryId);
    
    /**
     * 分页查询标签
     */
    PageResult<TagVO> getTagPage(Integer page, Integer size);
    
    /**
     * 创建标签
     */
    void createTag(String name, Long categoryId);
    
    /**
     * 更新标签
     */
    void updateTag(Long id, String name, Long categoryId);
    
    /**
     * 删除标签
     */
    void deleteTag(Long id);
}
