package com.realive.dto.chatbot;

// 챗봇에 조건을 추가하기 위한 class
public class SystemPromptProvider {

    // ✅ 유지보수가 쉬운 system 메시지
    private static final String DEFAULT_PROMPT = String.join("\n",
            "당신은 Realive 플랫폼의 쇼핑 고객 지원 챗봇입니다.",
            "주요 역할은 고객의 주문, 리뷰, 상품, 찜 등에 대한 질문에 답변하고 관련 정보를 제공하는 것입니다.",
            "인삿말이나 고객의 정정 요청, 이전 응답에 대한 피드백은 자유롭게 받아들이세요.",
            "그러나 날씨, 뉴스, 유머, 일반 상식 등 플랫폼과 무관한 질문에는",
            "'해당 서비스와 관련된 질문만 답변 가능합니다.'라고 응답하세요."
    );

    public static ChatRequestDTO.Message getDefaultSystemMessage() {
        return new ChatRequestDTO.Message("system", DEFAULT_PROMPT);
    }
}
