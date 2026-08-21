package com.synkork.backend.common.utils.LLMFunction;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class TikaFileService {

    private final MeetingLlmService meetingLlmService;
    
    // tạo 1 lần và dùng chung
    private final Tika tika = new Tika();

    public String convertClondinaryToString(String cloudinaryUrl) {
        try {
            URL url = new URL(cloudinaryUrl);
            String extractedText;

            // tải file
            try (InputStream inputStream = url.openStream()) {
                extractedText = tika.parseToString(inputStream);
            }


            if (extractedText.length() > 30000) {
                extractedText = extractedText.substring(0, 30000);
            }

            return meetingLlmService.summarizeGeneric(extractedText, LlmPrompts.MEETING_SUMMARY_MODELS);

        } catch (IOException e) {
            throw new RuntimeException("Có lỗi khi đọc và tóm tắt file đính kèm: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi văn bản: " + e.getMessage(), e);
        }
    }
}
