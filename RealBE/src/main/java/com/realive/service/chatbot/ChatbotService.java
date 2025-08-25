package com.realive.service.chatbot;

import com.realive.dto.chatbot.ChatRequestDTO;

import java.util.List;

public interface ChatbotService {
    String getChatbotReply(List<ChatRequestDTO.Message> messages);
}

