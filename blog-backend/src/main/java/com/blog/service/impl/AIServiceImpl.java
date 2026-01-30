package com.blog.service.impl;

import com.blog.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI 服务实现类
 * 
 * @author Blog System
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {
    
    @Value("${deepseek.api-key}")
    private String apiKey;
    
    @Value("${deepseek.api-url}")
    private String apiUrl;
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    
    public AIServiceImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void generateContent(String outline, Consumer<String> callback) throws IOException {
        String systemPrompt = "你是一个专业的写作助手，擅长根据大纲生成文章内容。\n\n" +
                "**严格的Markdown格式要求：**\n" +
                "1. 标题：# ## ### 后必须有空格\n" +
                "2. 每个标题后必须换行\n" +
                "3. 段落之间用空行分隔\n" +
                "4. 列表项：- 或 1. 后必须有空格\n" +
                "5. 代码块用 ``` 包裹";
        String message = "请根据以下大纲生成文章内容：\n\n" + outline;
        callDeepSeekAPI(message, systemPrompt, callback);
    }
    
    @Override
    public void continueWriting(String content, Consumer<String> callback) throws IOException {
        String systemPrompt = "你是一个专业的写作助手，擅长续写文章。\n\n" +
                "**格式要求：**\n" +
                "1. 保持与原文一致的Markdown格式\n" +
                "2. 标题后有空格并换行\n" +
                "3. 段落间用空行分隔";
        String message = "请帮我续写以下内容：\n\n" + content;
        callDeepSeekAPI(message, systemPrompt, callback);
    }
    
    @Override
    public void optimizeContent(String content, Consumer<String> callback) throws IOException {
        String systemPrompt = "你是一个专业的写作助手，擅长优化文章内容。\n\n" +
                "**格式要求：**\n" +
                "1. 使用标准Markdown格式\n" +
                "2. 标题：# 后有空格并换行\n" +
                "3. 段落间用空行分隔";
        String message = "请帮我优化以下内容：\n\n" + content;
        callDeepSeekAPI(message, systemPrompt, callback);
    }
    
    @Override
    public void articleQA(String articleContent, String question, Consumer<String> callback) throws IOException {
        String systemPrompt = "你是一个智能助手，正在帮助用户理解一篇文章。\n\n" +
                "**格式要求：**\n" +
                "1. 使用Markdown格式\n" +
                "2. 标题后有空格\n" +
                "3. 列表项格式规范";
        String message = "文章内容：\n\n" + articleContent + "\n\n用户问题：" + question;
        callDeepSeekAPI(message, systemPrompt, callback);
    }
    
    @Override
    public void extractSummary(String content, Consumer<String> callback) throws IOException {
        String systemPrompt = "你是一个专业的文章摘要提取助手。\n\n" +
                "**严格要求：**\n" +
                "1. 直接输出摘要内容，不要添加摘要：前缀\n" +
                "2. 摘要必须是纯文本，不使用任何Markdown格式\n" +
                "3. 摘要长度必须控制在300字以内\n" +
                "4. 提取文章的核心内容和要点\n" +
                "5. 语言简洁明了，逻辑清晰";
        String message = "请根据以下文章内容提取一个简洁的摘要（不超过300字，纯文本格式）：\n\n" + content;
        callDeepSeekAPI(message, systemPrompt, callback);
    }
    
    /**
     * 调用 DeepSeek API
     */
    private void callDeepSeekAPI(String message, String systemPrompt, Consumer<String> callback) throws IOException {
        log.info("开始调用 DeepSeek API");
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        requestBody.put("stream", true);
        
        // 转换为JSON
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        // 构建请求
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
        
        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            // 处理流式响应
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    throw new IOException("Response body is null");
                }
                
                // 逐行读取响应
                try (okio.BufferedSource source = body.source()) {
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line == null) break;
                        
                        // 跳过空行
                        if (line.isEmpty()) {
                            continue;
                        }
                        
                        // 处理data行
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) {
                                break;
                            }
                            
                            try {
                                // 解析JSON
                                JsonNode rootNode = objectMapper.readTree(data);
                                JsonNode choicesNode = rootNode.path("choices");
                                if (choicesNode.isArray() && choicesNode.size() > 0) {
                                    JsonNode deltaNode = choicesNode.get(0).path("delta");
                                    String content = deltaNode.path("content").asText();
                                    if (!content.isEmpty()) {
                                        callback.accept(content);
                                    }
                                }
                            } catch (Exception e) {
                                log.error("解析响应失败", e);
                            }
                        }
                    }
                }
            }
        }
        
        log.info("DeepSeek API 调用完成");
    }
}
