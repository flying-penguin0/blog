package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.entity.Category;
import com.blog.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 * 
 * @author Blog System
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取所有分类
     */
    List<CategoryVO> getAllCategories();
    
    /**
     * 分页查询分类
     */
    PageResult<CategoryVO> getCategoryPage(Integer page, Integer size, String name);
    
    /**
     * 创建分类
     */
    void createCategory(String name, String description);
    
    /**
     * 更新分类
     */
    void updateCategory(Long id, String name, String description);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
