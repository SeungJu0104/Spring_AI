package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Hotel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceParameterizedTypeReference {
  // ##### 필드 #####
  private ChatClient chatClient;

  // ##### 생성자 #####
  public AiServiceParameterizedTypeReference(ChatClient.Builder chatClientBuilder) {
    chatClient = chatClientBuilder.build();
  }

  // ##### 메소드 #####
  public List<Hotel> genericBeanOutputConverterLowLevel(String cities) {
    // 구조화된 출력 변환기 생성
    BeanOutputConverter<List<Hotel>> beanOutputConverter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<List<Hotel>>() {}); // 익명객체 구현
    // 단순히 List<Hotel> 객체만 생성하면 컴파일 시에만 객체가 생성되므로, format 등에 사용될 때는 객체가 없어 에러 발생 
    // -> 그래서 ParamterizedTypeReference 를 상속받은 구현 객체 생성해 해결
    
    // 프롬프트 템플릿 생성
    PromptTemplate promptTemplate = new PromptTemplate("""
        다음 도시들에서 유명한 호텔 3개를 출력하세요.
        {cities}
        {format}
        """);
    // 프롬프트 생성
    Prompt prompt = promptTemplate.create(Map.of(
        "cities", cities, 
        "format", beanOutputConverter.getFormat()));
    // LLM의 JSON 출력 얻기
    String json = chatClient.prompt(prompt)
        .call()
        .content();
    // JSON을 List<Hotel>로 매핑해서 변환
    List<Hotel> hotelList = beanOutputConverter.convert(json);
    return hotelList;
  }
  
  public List<Hotel> genericBeanOutputConverterHighLevel(String cities) {
    List<Hotel> hotelList = chatClient.prompt().user("""
        다음 도시들에서 유명한 호텔 3개를 출력하세요.
        %s
        """.formatted(cities))
        .call()
        .entity(new ParameterizedTypeReference<List<Hotel>>() {});
    // 단순히 List<Hotel> 객체만 생성하면 컴파일 시에만 객체가 생성되므로, format 등에 사용될 때는 객체가 없어 에러 발생 
    // -> 그래서 ParamterizedTypeReference 를 상속받은 구현 객체 생성해 해결
    return hotelList;
  }
}
