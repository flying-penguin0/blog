package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CategoryDTO;
import com.blog.service.CategoryService;
import com.blog.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 * 
 * @author Blog System
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有分类（不分页，用于前台展示）
     */
    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 分页查询分类（用于后台管理）
     */
    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Result<PageResult<CategoryVO>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        PageResult<CategoryVO> result = categoryService.getCategoryPage(page, size, name);
        return Result.success(result);
    }
    
    /**
     * 创建分类
     */
    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Void> createCategory(@RequestBody CategoryDTO dto) {
        categoryService.createCategory(dto.getName(), dto.getDescription());
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新分类
     */
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.updateCategory(id, dto.getName(), dto.getDescription());
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除分类
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
