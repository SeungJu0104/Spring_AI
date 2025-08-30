package com.example.demo.service;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiServiceByChatClient {
  // ##### 필드 #####
  private ChatClient chatClient;
  
  // ##### 생성자 #####
  public AiServiceByChatClient(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  // ##### 메소드 #####
  public String generateText(String question) {
    String answer = chatClient.prompt()
        .system("사용자 질문에 대해 한국어로 답변을 해야 합니다.") // 시스템 메세지
        .user(question) // 유저 메세지
        .options(ChatOptions.builder() // ChatOptions 객체
            .temperature(0.3)
            .maxTokens(1000)
            .build()
        )
        .call() // 동기로 응답 리턴 -> 그래서 String 으로 리턴 가능
        .content();
    
    return answer;
  }

  public Flux<String> generateStreamText(String question) {
    Flux<String> fluxString = chatClient.prompt()
        .system("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
        .user(question)
        .options(ChatOptions.builder()
            .temperature(0.3)
            .maxTokens(1000)
            .build()
        )        
        .stream() // 비동기로 응답 리턴 -> 그래서 Flux 로 리턴
        .content();
  
    return fluxString;
  }
}
