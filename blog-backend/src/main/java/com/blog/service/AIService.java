package com.blog.service;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * AI 服务接口
 * 
 * @author Blog System
 */
public interface AIService {
    
    /**
     * 生成文章内容
     */
    void generateContent(String outline, Consumer<String> callback) throws IOException;
    
    /**
     * AI 续写
     */
    void continueWriting(String content, Consumer<String> callback) throws IOException;
    
    /**
     * 内容优化
     */
    void optimizeContent(String content, Consumer<String> callback) throws IOException;
    
    /**
     * 文章问答
     */
    void articleQA(String articleContent, String question, Consumer<String> callback) throws IOException;
    
    /**
     * AI 提取摘要
     */
    void extractSummary(String content, Consumer<String> callback) throws IOException;
}
