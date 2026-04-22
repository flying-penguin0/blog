package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.SensitiveWordDTO;
import com.blog.entity.SensitiveWord;
import com.blog.exception.BusinessException;
import com.blog.mapper.SensitiveWordMapper;
import com.blog.service.SensitiveWordService;
import com.blog.utils.SensitiveWordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现类
 * 
 * @author Blog System
 */
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {
    
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    
    @Override
    public Page<SensitiveWord> getPage(Integer pageNum, Integer pageSize, String word, Integer status) {
        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(word), SensitiveWord::getWord, word)
               .eq(status != null, SensitiveWord::getStatus, status)
               .orderByDesc(SensitiveWord::getCreateTime);
        
        return sensitiveWordMapper.selectPage(page, wrapper);
    }
    
    @Override
    public void create(SensitiveWordDTO dto) {
        // 检查敏感词是否已存在
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getWord, dto.getWord());
        Long count = sensitiveWordMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new BusinessException("敏感词已存在");
        }
        
        SensitiveWord sensitiveWord = new SensitiveWord();
        BeanUtils.copyProperties(dto, sensitiveWord);
        
        // 设置默认值
        if (sensitiveWord.getStatus() == null) {
            sensitiveWord.setStatus(1);
        }
        
        sensitiveWordMapper.insert(sensitiveWord);
        
        // 刷新缓存
        refreshCache();
    }
    
    @Override
    public void update(Long id, SensitiveWordDTO dto) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        if (sensitiveWord == null) {
            throw new BusinessException("敏感词不存在");
        }
        
        // 如果修改了敏感词内容，检查是否与其他敏感词重复
        if (dto.getWord() != null && !sensitiveWord.getWord().equals(dto.getWord())) {
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, dto.getWord())
                   .ne(SensitiveWord::getId, id);
            Long count = sensitiveWordMapper.selectCount(wrapper);
            
            if (count > 0) {
                throw new BusinessException("敏感词已存在");
            }
        }
        
        // 只更新非空字段
        if (dto.getWord() != null) {
            sensitiveWord.setWord(dto.getWord());
        }
        if (dto.getStatus() != null) {
            sensitiveWord.setStatus(dto.getStatus());
        }
        
        sensitiveWordMapper.updateById(sensitiveWord);
        
        // 刷新缓存
        refreshCache();
    }
    
    @Override
    public void delete(Long id) {
        sensitiveWordMapper.deleteById(id);
        
        // 刷新缓存
        refreshCache();
    }
    
    @Override
    public List<String> getAllEnabledWords() {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getStatus, 1); // 只获取启用的敏感词
        
        List<SensitiveWord> words = sensitiveWordMapper.selectList(wrapper);
        return words.stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());
    }
    
    @Override
    public List<SensitiveWord> getEnabledWords() {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getStatus, 1); // 只获取启用的敏感词
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        return sensitiveWordMapper.selectList(wrapper);
    }
    
    @Override
    public boolean containsSensitiveWord(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        return sensitiveWordUtil.containsSensitiveWord(content);
    }
    
    /**
     * 刷新缓存（私有方法，内部使用）
     */
    private void refreshCache() {
        // 重新加载敏感词到工具类
        sensitiveWordUtil.reloadSensitiveWords(getAllEnabledWords());
    }
}
