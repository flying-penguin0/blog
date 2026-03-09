package com.blog.controller;

import com.blog.dto.AIRequestDTO;
import com.blog.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;

/**
 * AI 控制器
 * 
 * @author Blog System
 */
@Tag(name = "AI 功能")
@RestController
@RequestMapping("/ai")
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * 生成文章内容
     */
    @Operation(summary = "生成文章内容")
    @PostMapping(value = "/generate-content")
    public void generateContent(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        try (PrintWriter writer = response.getWriter()) {
            aiService.generateContent(dto.getOutline(), content -> {
                try {
                    writer.write("data:" + escapeJson(content) + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.write("data:[DONE]\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * AI 续写
     */
    @Operation(summary = "AI 续写")
    @PostMapping(value = "/continue-writing")
    public void continueWriting(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        try (PrintWriter writer = response.getWriter()) {
            aiService.continueWriting(dto.getContent(), content -> {
                try {
                    writer.write("data:" + escapeJson(content) + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.write("data:[DONE]\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 内容优化
     */
    @Operation(summary = "内容优化")
    @PostMapping(value = "/optimize")
    public void optimizeContent(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        try (PrintWriter writer = response.getWriter()) {
            aiService.optimizeContent(dto.getContent(), content -> {
                try {
                    writer.write("data:" + escapeJson(content) + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.write("data:[DONE]\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 文章问答
     */
    @Operation(summary = "文章问答")
    @PostMapping(value = "/article-qa")
    public void articleQA(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        try (PrintWriter writer = response.getWriter()) {
            aiService.articleQA(dto.getArticleContent(), dto.getMessage(), content -> {
                try {
                    writer.write("data:" + escapeJson(content) + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.write("data:[DONE]\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * AI 提取摘要
     */
    @Operation(summary = "AI 提取摘要")
    @PostMapping(value = "/extract-summary")
    public void extractSummary(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        try (PrintWriter writer = response.getWriter()) {
            aiService.extractSummary(dto.getContent(), content -> {
                try {
                    writer.write("data:" + escapeJson(content) + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writer.write("data:[DONE]\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
