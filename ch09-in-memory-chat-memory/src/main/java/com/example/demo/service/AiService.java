package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {
  // ##### 필드 ##### 
  private ChatClient chatClient;
  
  // ##### 생성자 #####
  public AiService(
      ChatMemory chatMemory, 
      ChatClient.Builder chatClientBuilder) {   
      this.chatClient = chatClientBuilder
          .defaultAdvisors(
              //MessageChatMemoryAdvisor.builder(chatMemory).build(), // 사용자 메세지, 어시스턴트 메세지에 추가
              PromptChatMemoryAdvisor.builder(chatMemory).build(), // 시스템 메세지에 추가
              new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1)
          )
          .build();
  }
  
  
  // ##### 메소드 #####
  public String chat(String userText, String conversationId) {
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
