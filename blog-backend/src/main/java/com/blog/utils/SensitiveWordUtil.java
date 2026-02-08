package com.blog.utils;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 敏感词过滤工具类
 * 使用 DFA 算法实现高效的敏感词检测
 * 
 * @author Blog System
 */
@Component
public class SensitiveWordUtil {
    
    /**
     * 敏感词库（DFA算法的HashMap结构）
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();
    
    /**
     * 是否是敏感词的结束标识
     */
    private static final String IS_END = "isEnd";
    
    /**
     * 初始化敏感词库
     */
    public SensitiveWordUtil() {
        // 初始化敏感词列表
        Set<String> sensitiveWords = new HashSet<>();
        
        // 添加常见敏感词（示例）
        sensitiveWords.add("傻逼");
        sensitiveWords.add("fuck");
        sensitiveWords.add("shit");
        sensitiveWords.add("垃圾");
        sensitiveWords.add("废物");
        sensitiveWords.add("白痴");
        sensitiveWords.add("智障");
        sensitiveWords.add("脑残");
        sensitiveWords.add("sb");
        sensitiveWords.add("SB");
        sensitiveWords.add("傻B");
        sensitiveWords.add("傻b");
        sensitiveWords.add("煞笔");
        sensitiveWords.add("沙比");
        sensitiveWords.add("妈的");
        sensitiveWords.add("草泥马");
        sensitiveWords.add("尼玛");
        sensitiveWords.add("你妈");
        sensitiveWords.add("操");
        sensitiveWords.add("日");
        sensitiveWords.add("艹");
        sensitiveWords.add("傻");
        
        // 初始化敏感词库
        initSensitiveWordMap(sensitiveWords);
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
