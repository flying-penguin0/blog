package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.SensitiveWordDTO;
import com.blog.entity.SensitiveWord;

import java.util.List;

/**
 * 敏感词服务接口
 * 
 * @author Blog System
 */
public interface SensitiveWordService {
    
    /**
     * 分页查询敏感词
     */
    Page<SensitiveWord> getPage(Integer pageNum, Integer pageSize, String word, Integer status);
    
    /**
     * 创建敏感词
     */
    void create(SensitiveWordDTO dto);
    
    /**
     * 更新敏感词
     */
    void update(Long id, SensitiveWordDTO dto);
    
    /**
     * 删除敏感词
     */
    void delete(Long id);
    
    /**
     * 获取所有启用的敏感词
     */
    List<String> getAllEnabledWords();
    
    /**
     * 获取所有启用的敏感词实体
     */
    List<SensitiveWord> getEnabledWords();
    
    /**
     * 检测文本是否包含敏感词
     */
    boolean containsSensitiveWord(String content);
}
