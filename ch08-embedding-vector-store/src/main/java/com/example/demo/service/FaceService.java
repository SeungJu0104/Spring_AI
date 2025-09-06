package com.example.demo.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FaceService {
  // ##### 필드 #####
  @Autowired 
  private JdbcTemplate jdbcTemplate;
  private WebClient webClient;
  
  // ##### 생성자 #####
  public FaceService(WebClient.Builder webClientBuilder) {
    webClient = webClientBuilder.build();
  }
  
  // ##### 메소드 #####
  public float[] getFaceVector(MultipartFile mf) throws IOException {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", mf.getBytes())
      .filename(mf.getOriginalFilename())
      .contentType(MediaType.valueOf(mf.getContentType()));
    MultiValueMap<String, HttpEntity<?>> multipartForm = builder.build();
    
    FaceEmbedApiResponse response = webClient.post()
        .uri("http://localhost:50001/get-face-vector")
        .body(BodyInserters.fromMultipartData(multipartForm))
        .retrieve()
        .bodyToMono(FaceEmbedApiResponse.class)
        .block();
    
    float[] vector = response.vector();
    return vector;
  }
  
  public record FaceEmbedApiResponse(float[] vector) {
  }  
  
//  @Data
//  public class FaceEmbedApiResponse {
//	  private float[] vector;
//  }  
  // Spring AI 문서에는 Record 타입 사용하도록 되어있으나 lombok 이용해서 사용하는 것도 가능하다.
  
  public void addFace(String personName, MultipartFile mf) throws IOException {  
      // 얼굴 임베딩
      float[] vector = getFaceVector(mf);
      
      // 벡터 저장소에 저장
      String strVector = Arrays.toString(vector).replace(" ", "");
      String sql = """
          INSERT INTO face_vector_store (content, embedding) 
          VALUES (?, ?::vector)
          """;
      jdbcTemplate.update(sql, personName, strVector);
  }
  
  public String findFace(MultipartFile mf) throws IOException {
    // 얼굴 임베딩
    float[] vector = getFaceVector(mf);
    String strVector = Arrays.toString(vector).replace(" ", "");
    // 유사한 얼굴 찾기(<=>): L2 정규화가 되어있을 경우 L2 거리, 0~2, 작을수록 유사
    String sql = """
        SELECT content, (embedding <=> ?::vector) AS similarity
        FROM face_vector_store 
        ORDER BY embedding <=> ?::vector 
        LIMIT 3
        """;
    // 검색 결과를 출력해보기
    List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, strVector, strVector);
    for(Map<String, Object> map : list) {
      String personName = (String) map.get("content");
      Double similarity = (Double) map.get("similarity");
      log.info("{} (L2 거리: {})", personName, similarity);
    }    
    
    // 검색 결과에서 거리가 가장 짧은 벡터의 유사도가 임계값 0.3 이상일 경우
    double similarity = (Double) list.get(0).get("similarity");
    if(similarity > 0.3) {
      return "등록된 사람이 아닙니다.";
    }
    
    // 거리가 가장 짧은 사람의 이름 반환
    return (String) list.get(0).get("content");
  }
 
}
