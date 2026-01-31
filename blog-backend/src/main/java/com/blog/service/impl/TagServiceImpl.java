package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 * 
 * @author Blog System
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<TagVO> getAllTags(Long categoryId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Tag::getCategoryId, categoryId);
        }
        List<Tag> tags = this.list(wrapper);
        return tags.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<TagVO> getTagPage(Integer page, Integer size, String name, Long categoryId) {
        Page<Tag> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        
        // 添加搜索条件
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Tag::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Tag::getCategoryId, categoryId);
        }
        
        Page<Tag> result = this.page(pageParam, wrapper);
        
        List<TagVO> tagVOList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(tagVOList, result.getTotal(), page, size);
    }
    
    @Override
    public void createTag(String name, Long categoryId) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCategoryId(categoryId);
        this.save(tag);
    }
    
    @Override
    public void updateTag(Long id, String name, Long categoryId) {
        Tag tag = this.getById(id);
        if (tag != null) {
            tag.setName(name);
            tag.setCategoryId(categoryId);
            this.updateById(tag);
        }
    }
    
    @Override
    public void deleteTag(Long id) {
        this.removeById(id);
    }
    
    /**
     * 转换为 VO
     */
    private TagVO convertToVO(Tag tag) {
        TagVO vo = BeanUtil.copyProperties(tag, TagVO.class);
        
        // 使用 SQL 查询统计该标签下的文章数量
        Integer count = baseMapper.countArticlesByTagId(tag.getId());
        vo.setArticleCount(count != null ? count : 0);
        
        // 获取分类名称
        if (tag.getCategoryId() != null) {
            Category category = categoryMapper.selectById(tag.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        return vo;
    }
}
