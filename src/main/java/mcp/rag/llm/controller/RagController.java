package mcp.rag.llm.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private final ChatClient chatClient;

    public RagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem("제공된 문서 내용만 근거로 답해. 문서에 없으면 '문서에 없는 내용입니다'라고 답해.")
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(4)                   // 비슷한 청크 4개 가져오기
                                .similarityThreshold(0.5)  // 유사도 0.5 이상만
                                .build())
                        .build())
                .build();
    }

    @GetMapping("/rag")
    public String rag(@RequestParam String q) {
        return chatClient.prompt().user(q).call().content();
    }
}
