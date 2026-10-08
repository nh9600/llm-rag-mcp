package mcp.rag.llm.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mcp.rag.llm.tools.DateTimeTools;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                // 1. 시스템 프롬프트: 모든 요청에 공통 적용
                .defaultSystem("너는 친절한 한국어 개발 도우미야. 모르는 내용은 모른다고 답해.")
                // 2. 대화 메모리: 같은 sessionId면 이전 대화를 기억
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    // 일반 응답
    @GetMapping("/chat")
    public String chat(@RequestParam String q,
                       @RequestParam(defaultValue = "default") String sessionId) {
        return chatClient.prompt()
                .user(q)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }

    // 3. 스트리밍 응답 (답변이 조각조각 흘러나옴)
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)// Server-Sent Events(SSE) 형식
    public Flux<String> stream(@RequestParam String q,
                               @RequestParam(defaultValue = "default") String sessionId) {// 사용자의 대화 세션 ID
        return chatClient.prompt()
                .user(q)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))// 세션 ID의 대화내용 기억
                .stream()
                .content();
    }
    
    /**
     * Tool Calling API
     * - LLM에 호출 가능한 도구(DateTimeTools) 목록을 함께 전달
     * - LLM이 질문을 분석해 필요한 도구를 선택하면 Spring AI가 자동 실행 후 결과를 LLM에 재전달
     * - LLM은 도구 실행 결과를 근거로 최종 답변 생성
     * API: GET /tool?q={질문}
     */
    @GetMapping("/tool")
    public String tool(@RequestParam String q) {
        return chatClient.prompt()
                .user(q)
                .tools(new DateTimeTools())
                .call()
                .content();
    }
}