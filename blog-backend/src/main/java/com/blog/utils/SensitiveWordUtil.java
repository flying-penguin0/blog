package com.blog.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.SensitiveWord;
import com.blog.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 敏感词过滤工具类
 * 使用 DFA 算法实现高效的敏感词检测
 * 
 * @author Blog System
 */
@Component
public class SensitiveWordUtil {
    
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    
    /**
     * 敏感词库（DFA算法的HashMap结构）
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();
    
    /**
     * 是否是敏感词的结束标识
     */
    private static final String IS_END = "isEnd";
    
    /**
     * 初始化敏感词库 - 从数据库加载
     */
    @PostConstruct
    public void init() {
        loadSensitiveWordsFromDatabase();
    }
    
    /**
     * 从数据库加载敏感词
     */
    private void loadSensitiveWordsFromDatabase() {
        try {
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getStatus, 1); // 只加载启用的敏感词
            
            List<SensitiveWord> words = sensitiveWordMapper.selectList(wrapper);
            Set<String> sensitiveWords = words.stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toSet());

            // 如果数据库为空，使用默认敏感词
            if (sensitiveWords.isEmpty()) {
                sensitiveWords = getDefaultSensitiveWords();
            }
            
            // 初始化敏感词库
            initSensitiveWordMap(sensitiveWords);
            
            System.out.println("敏感词库加载成功，共加载 " + sensitiveWords.size() + " 个敏感词");
        } catch (Exception e) {
            System.err.println("从数据库加载敏感词失败，使用默认敏感词库: " + e.getMessage());
            // 如果数据库加载失败，使用默认敏感词
            initSensitiveWordMap(getDefaultSensitiveWords());
        }
    }
    
    /**
     * 获取默认敏感词列表
     */
    private Set<String> getDefaultSensitiveWords() {
        Set<String> sensitiveWords = new HashSet<>();
        return sensitiveWords;
    }
    
    /**
     * 重新加载敏感词（用于动态更新）
     */
    public synchronized void reloadSensitiveWords(List<String> words) {
        Set<String> sensitiveWords = new HashSet<>(words);
        sensitiveWordMap.clear();
        initSensitiveWordMap(sensitiveWords);
        System.out.println("敏感词库已刷新，当前共 " + sensitiveWords.size() + " 个敏感词");
    }
    
    /**
     * 初始化敏感词库（构建DFA算法模型）
     */
    private void initSensitiveWordMap(Set<String> sensitiveWords) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return;
        }
        
        Map<String, Object> nowMap;
        Map<String, Object> newWordMap;
        
        for (String word : sensitiveWords) {
            nowMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                char keyChar = word.charAt(i);
                Object wordMap = nowMap.get(String.valueOf(keyChar));
                
                if (wordMap != null) {
                    nowMap = (Map<String, Object>) wordMap;
                } else {
                    newWordMap = new HashMap<>();
                    newWordMap.put(IS_END, "0");
                    nowMap.put(String.valueOf(keyChar), newWordMap);
                    nowMap = newWordMap;
                }
                
                if (i == word.length() - 1) {
                    nowMap.put(IS_END, "1");
                }
            }
        }
    }
    
    /**
     * 检查文本中是否包含敏感词
     * 
     * @param text 待检查的文本
     * @return true-包含敏感词，false-不包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查文本从指定位置开始是否包含敏感词
     * 
     * @param text 待检查的文本
     * @param beginIndex 开始位置
     * @return 敏感词的长度，0表示不包含敏感词
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        boolean flag = false;
        int matchFlag = 0;
        Map nowMap = sensitiveWordMap;
        
        for (int i = beginIndex; i < text.length(); i++) {
            char word = text.charAt(i);
            nowMap = (Map) nowMap.get(String.valueOf(word));
            
            if (nowMap != null) {
                matchFlag++;
                if ("1".equals(nowMap.get(IS_END))) {
                    flag = true;
                }
            } else {
                break;
            }
        }
        
        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }
}
