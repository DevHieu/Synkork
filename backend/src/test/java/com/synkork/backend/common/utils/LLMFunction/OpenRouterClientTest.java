package com.synkork.backend.common.utils.LLMFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenRouterClientTest {

    private final OpenRouterClient client = new OpenRouterClient(new ObjectMapper());

    @Test
    void testParseJsonOrFallback_PlainJson() {
        String input = "{\"summary\":\"Cuộc họp tốt\",\"keyPoints\":[],\"actionItems\":[]}";
        String result = client.parseJsonOrFallback(input, "{}");
        assertEquals(input, result);
    }

    @Test
    void testParseJsonOrFallback_MarkdownCodeFence() {
        String input = "```json\n{\"summary\":\"Cuộc họp tốt\",\"keyPoints\":[],\"actionItems\":[]}\n```";
        String result = client.parseJsonOrFallback(input, "{}");
        assertEquals("{\"summary\":\"Cuộc họp tốt\",\"keyPoints\":[],\"actionItems\":[]}", result);
    }

    @Test
    void testParseJsonOrFallback_WithThinkingOrPrefixText() {
        String input = "Dưới đây là kết quả:\n```json\n{\"summary\":\"OK\",\"keyPoints\":[\"p1\"],\"actionItems\":[]}\n```\nChúc bạn một ngày tốt lành!";
        String result = client.parseJsonOrFallback(input, "{}");
        assertEquals("{\"summary\":\"OK\",\"keyPoints\":[\"p1\"],\"actionItems\":[]}", result);
    }

    @Test
    void testParseJsonOrFallback_InvalidJsonFallback() {
        String input = "Không thể tìm thấy JSON hợp lệ";
        String result = client.parseJsonOrFallback(input, "{\"fallback\":true}");
        assertEquals("{\"fallback\":true}", result);
    }
}
