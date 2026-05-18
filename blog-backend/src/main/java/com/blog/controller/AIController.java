package com.blog.controller;

import com.blog.dto.AIRequestDTO;
import com.blog.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;

@Tag(name = "AI 功能")
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "生成文章内容")
    @PostMapping("/generate-content")
    public void generateContent(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        streamResponse(response, writer -> aiService.generateContent(dto.getOutline(), content -> writeEvent(writer, content)));
    }

    @Operation(summary = "AI 续写")
    @PostMapping("/continue-writing")
    public void continueWriting(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        streamResponse(response, writer -> aiService.continueWriting(dto.getContent(), content -> writeEvent(writer, content)));
    }

    @Operation(summary = "内容优化")
    @PostMapping("/optimize")
    public void optimizeContent(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        streamResponse(response, writer -> aiService.optimizeContent(dto.getContent(), content -> writeEvent(writer, content)));
    }

    @Operation(summary = "文章问答")
    @PostMapping("/article-qa")
    public void articleQA(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        streamResponse(response, writer -> aiService.articleQA(dto.getArticleContent(), dto.getMessage(), content -> writeEvent(writer, content)));
    }

    @Operation(summary = "AI 提取摘要")
    @PostMapping("/extract-summary")
    public void extractSummary(@Valid @RequestBody AIRequestDTO dto, HttpServletResponse response) {
        streamResponse(response, writer -> aiService.extractSummary(dto.getContent(), content -> writeEvent(writer, content)));
    }

    private void streamResponse(HttpServletResponse response, StreamingTask task) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try (PrintWriter writer = response.getWriter()) {
            task.run(writer);
            writeDoneEvent(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeEvent(PrintWriter writer, String content) {
        writePayload(writer, new AIStreamEvent(content, false));
    }

    private void writeDoneEvent(PrintWriter writer) {
        writePayload(writer, new AIStreamEvent("", true));
    }

    private void writePayload(PrintWriter writer, AIStreamEvent payload) {
        try {
            writer.write("data:" + objectMapper.writeValueAsString(payload) + "\n\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface StreamingTask {
        void run(PrintWriter writer) throws Exception;
    }

    private record AIStreamEvent(String content, boolean done) {
    }
}
