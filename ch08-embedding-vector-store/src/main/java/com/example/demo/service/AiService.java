package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {
  // ##### 필드 #####
  @Autowired
  private EmbeddingModel embeddingModel;

  @Autowired
  private VectorStore vectorStore;

  // ##### 메소드 #####
  public void textEmbedding(String question) { // 사용자가 입력한 질문을 매개변수로 받는다.
    // 임베딩하기
    EmbeddingResponse response = embeddingModel.embedForResponse(List.of(question));
    // List 형태로 embedForResponse 메소드 이용해 모델 입력할 텍스트 목록을 넘겨주고, 결과를 벡터와 메타데이터가 포함된 EmbeddingResponse로 반환.
    
    // 임베딩 모델 정보 얻기
    EmbeddingResponseMetadata metadata = response.getMetadata();
    log.info("모델 이름: {}", metadata.getModel());
    log.info("모델의 임베딩 차원: {}", embeddingModel.dimensions());

    // 임베딩 결과 얻기
    Embedding embedding = response.getResults().get(0);
    log.info("벡터 차원: {}", embedding.getOutput().length);
    log.info("벡터: {}", embedding.getOutput());
  }

  // public void textEmbedding(String question) {
  //   // 임베딩하기
  //   float[] vector = embeddingModel.embed(question);
  //   log.info("벡터 차원: {}", vector.length);
  //   log.info("벡터: {}", vector);
  // }

  public void addDocument() {
    // Document 목록 생성
    List<Document> documents = List.of(
        new Document("대통령 선거는 5년마다 있습니다.", Map.of("source", "헌법", "year", 1987)),
        new Document("대통령 임기는 4년입니다.", Map.of("source", "헌법", "year", 1980)),
        new Document("국회의원은 법률안을 심의·의결합니다.", Map.of("source", "헌법", "year", 1987)),
        new Document("자동차를 사용하려면 등록을 해야합니다.", Map.of("source", "자동차관리법")),
        new Document("대통령은 행정부의 수반입니다.", Map.of("source", "헌법", "year", 1987)),
        new Document("국회의원은 4년마다 투표로 뽑습니다.", Map.of("source", "헌법", "year", 1987)),
        new Document("승용차는 정규적인 점검이 필요합니다.", Map.of("source", "자동차관리법")));

    // 벡터 저장소에 저장
    vectorStore.add(documents);
  }

  public List<Document> searchDocument1(String question) {
    List<Document> documents = vectorStore.similaritySearch(question);
    return documents;
  }

  public List<Document> searchDocument2(String question) {
    List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(question)
            .topK(1)
            .similarityThreshold(0.4)
            .filterExpression("source == '헌법' && year >= 1987")
            .build());
    return documents;
  }

  // public List<Document> searchDocument2(String question) {
  //   FilterExpressionBuilder feb = new FilterExpressionBuilder();

  //   List<Document> documents = vectorStore.similaritySearch(
  //       SearchRequest.builder()
  //           .query(question) // 유사도 검색에 사용될 텍스트
  //           .topK(1) // 유사도가 높은 상위 K개를 지정하는 정수(기본은 4개)
  //           .similarityThreshold(0.4) // 0에서 1시이 double 값. 이 값보다 높은 유사도 가진 문서만 검색.
  //           .filterExpression(feb
  //               .and(
  //                   feb.eq("source", "헌법"),
  //                   feb.gte("year", 1987))
  //               .build())
  //			filterExpression은 메타데이터 검색 조건(where 절과 유사한 개념)
  //           .build());
  //   return documents;
  // }

  public void deleteDocument() {
    vectorStore.delete("source == '헌법' && year < 1987");
  }
}
