package com.synkork.backend.common.utils.LLMFunction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatEventLlmServiceTest {

    @Test
    void testContainsEventEnglish() {
        String[] keywords = {"concert", "meeting", "party"};
        assertTrue(ChatEventLlmService.containsEvent("I will join the concert tonight", keywords));
        assertFalse(ChatEventLlmService.containsEvent("I am going to school", keywords));
    }

    @Test
    void testContainsEventVietnamese() {
        String[] keywords = {"họp", "lịch", "hẹn"};
        assertTrue(ChatEventLlmService.containsEvent("mai có lịch họp nhé mọi người", keywords));
        assertTrue(ChatEventLlmService.containsEvent("Hẹn gặp lúc 9h", keywords));
        assertFalse(ChatEventLlmService.containsEvent("chúc mừng năm mới", keywords));
    }

    @Test
    void testContainsEventNullOrEmpty() {
        String[] keywords = {"concert"};
        assertFalse(ChatEventLlmService.containsEvent(null, keywords));
        assertFalse(ChatEventLlmService.containsEvent("concert", null));
        assertFalse(ChatEventLlmService.containsEvent("", keywords));
    }

    @Test
    void testDetectType() {
        // Event keywords (only)
        assertEquals(ChatEventLlmService.MessageType.EVENT, ChatEventLlmService.detectType("mai đi đá bóng"));
        assertEquals(ChatEventLlmService.MessageType.EVENT, ChatEventLlmService.detectType("Hôm nay có cuộc họp"));
        assertEquals(ChatEventLlmService.MessageType.EVENT, ChatEventLlmService.detectType("I have a meeting"));

        // Task keywords (only)
        assertEquals(ChatEventLlmService.MessageType.TASK, ChatEventLlmService.detectType("hãy nộp báo cáo"));
        assertEquals(ChatEventLlmService.MessageType.TASK, ChatEventLlmService.detectType("nhắc tôi mua sữa"));
        assertEquals(ChatEventLlmService.MessageType.TASK, ChatEventLlmService.detectType("Please finish the assignment"));

        // Note keywords (only)
        assertEquals(ChatEventLlmService.MessageType.NOTE, ChatEventLlmService.detectType("ghi chú wifi: 12345678"));
        assertEquals(ChatEventLlmService.MessageType.NOTE, ChatEventLlmService.detectType("lưu lại số điện thoại"));
        assertEquals(ChatEventLlmService.MessageType.NOTE, ChatEventLlmService.detectType("Remember to save the idea"));

        // Unknown
        assertEquals(ChatEventLlmService.MessageType.UNKNOWN, ChatEventLlmService.detectType("hello world"));
        assertEquals(ChatEventLlmService.MessageType.UNKNOWN, ChatEventLlmService.detectType("gửi email cho tôi"));
    }
}

