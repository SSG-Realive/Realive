package com.realive.controller.chatbot;

import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.dto.chatbot.ChatbotResponseDTO;
import com.realive.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatbotResponseDTO> getChatReply(@RequestBody List<ChatRequestDTO.Message> messages) {
        // GPT에게 사용자 전체 메시지 히스토리를 보내고 응답 받기
        String reply = chatbotService.getChatbotReply(messages);

        // 응답 DTO로 감싸서 반환
        ChatbotResponseDTO responseDTO = new ChatbotResponseDTO(reply);
        return ResponseEntity.ok(responseDTO);
    }
}
