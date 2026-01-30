package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.TagDTO;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 * 
 * @author Blog System
 */
@Tag(name = "标签管理")
@RestController
@RequestMapping("/tag")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取所有标签（不分页，用于前台展示）
     */
    @Operation(summary = "获取所有标签")
    @GetMapping("/list")
    public Result<List<TagVO>> getAllTags(@RequestParam(required = false) Long categoryId) {
        List<TagVO> tags = tagService.getAllTags(categoryId);
        return Result.success(tags);
    }
    
    /**
     * 分页查询标签（用于后台管理）
     */
    @Operation(summary = "分页查询标签")
    @GetMapping("/page")
    public Result<PageResult<TagVO>> getTagPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<TagVO> result = tagService.getTagPage(page, size);
        return Result.success(result);
    }
    
    /**
     * 创建标签
     */
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Void> createTag(@RequestBody TagDTO dto) {
        tagService.createTag(dto.getName(), dto.getCategoryId());
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新标签
     */
    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody TagDTO dto) {
        tagService.updateTag(id, dto.getName(), dto.getCategoryId());
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除标签
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除成功", null);
    }
}
