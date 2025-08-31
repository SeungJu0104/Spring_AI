package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {
  // ##### 필드 ##### 
  private ChatClient chatClient;

  // ##### 생성자 #####
  public AiService(
      JdbcChatMemoryRepository chatMemoryRepository,
      ChatClient.Builder chatClientBuilder) {    
    ChatMemory chatMemory = MessageWindowChatMemory.builder()
        .chatMemoryRepository(chatMemoryRepository)
        .maxMessages(100)
        .build();

    this.chatClient = chatClientBuilder
        .defaultAdvisors(
            PromptChatMemoryAdvisor.builder(chatMemory).build(),
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1)
        )
        .build();
  }    

  public String chat(String userText, String conversationId) {
	  // 컨트롤러에 세션 아이디를 사용하고 있지만 RDBMS 를 이용해서 이전 대화 저장할 경우, 사용자 고유의 다른 것을 사용해야한다.
	  // 실습이기 때문에 일단 세션 아이디 사용
    String answer = chatClient.prompt()
      .user(userText)
      .advisors(advisorSpec -> advisorSpec.param(
        ChatMemory.CONVERSATION_ID, conversationId
      ))
      .call()
      .content();
    return answer;
  }
}
