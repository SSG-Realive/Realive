package com.realive.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 현재는 축약형으로 만듬 필요시 확장
@Getter
@AllArgsConstructor
public class ChatbotResponseDTO {
    private String reply;
}