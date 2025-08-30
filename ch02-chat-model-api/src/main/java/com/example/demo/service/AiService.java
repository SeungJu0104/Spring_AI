package com.example.demo.service;

import java.io.IOException;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {
  // ##### 필드 #####
  @Autowired
  private ChatModel chatModel;

  // ##### 메소드 #####
  public String generateText(String question) {
    // 시스템 메시지 생성
    SystemMessage systemMessage = SystemMessage.builder()
        .text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
        .build();

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        .text(question)
        .build();
    
    // 대화 옵션 설정
    ChatOptions chatOptions = ChatOptions.builder()
        .model("gpt-4o-mini") // 사용할 특정 AI 모델 지정. 생략하면 ChatGPT 4 mini 사용.
        .temperature(0.3) // 생략 시 기본 값은 0.7 이다. 0에 근접할 수록 고정된 답변을 줄 확률을 높인다.
        .maxTokens(1000) // 응답의 토큰 수 제한.
        .build();

    // 프롬프트 생성
    Prompt prompt = Prompt.builder()
        .messages(systemMessage, userMessage) // List<Message>
        .chatOptions(chatOptions)
        .build();

    // LLM에게 요청하고 응답받기
    ChatResponse chatResponse = chatModel.call(prompt);
    AssistantMessage assistantMessage = chatResponse.getResult().getOutput(); // 기존 대화 기억에 사용.
    String answer = assistantMessage.getText();

    return answer;
  }

  public Flux<String> generateStreamText(String question) {
    // 시스템 메시지 생성
    SystemMessage systemMessage = SystemMessage.builder()
        .text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
        .build();

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        .text(question)
        .build();

    // 대화 옵션 설정
    ChatOptions chatOptions = ChatOptions.builder()
        .model("gpt-4o")
        .temperature(0.3)
        .maxTokens(1000)
        .build();

    // 프롬프트 생성
    Prompt prompt = Prompt.builder()
        .messages(systemMessage, userMessage)
        .chatOptions(chatOptions)
        .build();

    // LLM에게 요청하고 응답받기
    Flux<ChatResponse> fluxResponse = chatModel.stream(prompt);
    Flux<String> fluxString = fluxResponse.map(chatResponse -> { 
    // chatResponse 는 역직렬화가 불가능. Flux<String> 이용해 문자열로 변환.
      AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
      String chunk = assistantMessage.getText();
      if (chunk == null) chunk = "";
      return chunk;
    });

    return fluxString;
  }
}
