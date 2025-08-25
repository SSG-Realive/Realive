package com.realive.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDTO {
    private String model;
    private List<Message> messages;

    // Function Calling 관련 필드
    private List<FunctionDefinition> functions;
    private Object function_call; // "auto" 또는 { name: "함수명" }

    @Data
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
        private String name;

        // 2개짜리 생성자
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // 3개짜리 생성자
        public Message(String role, String content, String name) {
            this.role = role;
            this.content = content;
            this.name = name;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FunctionDefinition {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }

    // 단순 메시지 요청 (function 미포함)
    public static ChatRequestDTO of(String model, String userMessage) {
        return new ChatRequestDTO(model,
                List.of(new Message("user", userMessage)),
                null,
                "auto"
        );
    }

    // Function 포함 요청 (시스템 메시지는 별도 클래스로 관리)
    public static ChatRequestDTO withFunctions(String model, String userMessage, List<FunctionDefinition> functions) {
        return new ChatRequestDTO(model,
                List.of(
                        SystemPromptProvider.getDefaultSystemMessage(),
                        new Message("user", userMessage)
                ),
                functions,
                "auto"
        );
    }
}
