package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.blog.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 * 
 * @author Blog System
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Override
    public List<CategoryVO> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getCreateTime);
        List<Category> categories = this.list(wrapper);
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<CategoryVO> getCategoryPage(Integer page, Integer size) {
        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getCreateTime);
        Page<Category> result = this.page(pageParam, wrapper);
        
        List<CategoryVO> categoryVOs = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(categoryVOs, result.getTotal(), page, size);
    }
    
    @Override
    public void createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        this.save(category);
    }
    
    @Override
    public void updateCategory(Long id, String name, String description) {
        Category category = this.getById(id);
        if (category != null) {
            category.setName(name);
            category.setDescription(description);
            this.updateById(category);
        }
    }
    
    @Override
    public void deleteCategory(Long id) {
        this.removeById(id);
    }
    
    /**
     * 转换为 VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = BeanUtil.copyProperties(category, CategoryVO.class);
        // 使用 SQL 查询统计该分类下的文章数量
        Integer count = baseMapper.countArticlesByCategoryId(category.getId());
        vo.setArticleCount(count != null ? count : 0);
        return vo;
    }
}
