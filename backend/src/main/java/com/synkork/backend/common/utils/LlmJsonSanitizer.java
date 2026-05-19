package com.synkork.backend.common.utils;

final class LlmJsonSanitizer {

    private LlmJsonSanitizer() {
    }

    static String sanitize(String rawContent) {
        if (rawContent == null) {
            return "{}";
        }

        String trimmed = rawContent.trim();
        if (trimmed.isEmpty()) {
            return "{}";
        }

        String withoutFence = stripMarkdownFence(trimmed);
        String extractedJson = extractFirstJsonValue(withoutFence);

        return extractedJson != null ? extractedJson : withoutFence;
    }

    private static String stripMarkdownFence(String value) {
        String normalized = value.trim();
        if (!normalized.startsWith("```")) {
            return normalized;
        }

        int firstLineBreak = normalized.indexOf('\n');
        if (firstLineBreak < 0) {
            return normalized.replace("```", "").trim();
        }

        String body = normalized.substring(firstLineBreak + 1);
        int closingFence = body.lastIndexOf("```");
        if (closingFence >= 0) {
            body = body.substring(0, closingFence);
        }

        return body.trim();
    }

    private static String extractFirstJsonValue(String value) {
        int objectStart = value.indexOf('{');
        int arrayStart = value.indexOf('[');
        int start = findJsonStart(objectStart, arrayStart);

        if (start < 0) {
            return null;
        }

        char openingChar = value.charAt(start);
        char closingChar = openingChar == '{' ? '}' : ']';

        int depth = 0;
        boolean inString = false;
        boolean escaping = false;

        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);

            if (inString) {
                if (escaping) {
                    escaping = false;
                    continue;
                }
                if (current == '\\') {
                    escaping = true;
                    continue;
                }
                if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == openingChar) {
                depth++;
            } else if (current == closingChar) {
                depth--;
                if (depth == 0) {
                    return value.substring(start, index + 1).trim();
                }
            }
        }

        return null;
    }

    private static int findJsonStart(int objectStart, int arrayStart) {
        if (objectStart < 0) {
            return arrayStart;
        }
        if (arrayStart < 0) {
            return objectStart;
        }
        return Math.min(objectStart, arrayStart);
    }
}
