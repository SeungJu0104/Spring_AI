package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.AdvisorA;
import com.example.demo.advisor.AdvisorB;
import com.example.demo.advisor.AdvisorC;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService1 {
  // ##### 필드 #####
  private ChatClient chatClient;

  // ##### 생성자 #####
  public AiService1(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
        .defaultAdvisors(
            new AdvisorA(), // order 반환 값이 AdvisorA가 더 작으므로, AdvisorB 보다 먼저 실행.
            new AdvisorB())
        .build();
  }

  // ##### 메소드 #####
  public String advisorChain1(String question) {
    String response = chatClient.prompt()
        .advisors(new AdvisorC())
        .user(question)
        .call()
        .content();
    return response;
  }
  
  
  // Web MVC 에서는 실행 X. Spring AI의 Advisor는 Reactive Web 기반에서만 사용 가능. 
  public Flux<String> advisorChain2(String question) {
    Flux<String> response = chatClient.prompt()
        .advisors(new AdvisorC())
        .user(question)
        .stream()
        .content();
    return response;
  }  
}
