package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.SensitiveWordDTO;
import com.blog.entity.SensitiveWord;
import com.blog.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词管理控制器
 * 
 * @author Blog System
 */
@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive-words")
public class SensitiveWordController {
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Operation(summary = "分页查询敏感词")
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    public Result<PageResult<SensitiveWord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String word,
            @RequestParam(required = false) Integer status) {
        
        Page<SensitiveWord> page = sensitiveWordService.getPage(pageNum, pageSize, word, status);
        
        PageResult<SensitiveWord> pageResult = new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }
    
    @Operation(summary = "创建敏感词")
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<Void> create(@Valid @RequestBody SensitiveWordDTO dto) {
        // 创建时必须提供敏感词
        if (dto.getWord() == null || dto.getWord().trim().isEmpty()) {
            return Result.error("敏感词不能为空");
        }
        sensitiveWordService.create(dto);
        return Result.success();
    }
    
    @Operation(summary = "更新敏感词")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SensitiveWordDTO dto) {
        sensitiveWordService.update(id, dto);
        return Result.success();
    }
    
    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.success();
    }
    
    @Operation(summary = "获取所有启用的敏感词")
    @GetMapping("/enabled")
    public Result<List<SensitiveWord>> getEnabledWords() {
        List<SensitiveWord> words = sensitiveWordService.getEnabledWords();
        return Result.success(words);
    }
    
    @Operation(summary = "检测文本是否包含敏感词")
    @PostMapping("/check")
    public Result<Boolean> checkSensitiveWord(@RequestBody String content) {
        boolean hasSensitive = sensitiveWordService.containsSensitiveWord(content);
        return Result.success(hasSensitive);
    }
}
