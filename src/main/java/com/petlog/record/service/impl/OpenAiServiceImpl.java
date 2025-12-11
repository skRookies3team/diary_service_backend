package com.petlog.record.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlog.record.dto.response.AiDiaryResponse;
import com.petlog.record.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    // 시스템 프롬프트: AI의 역할 정의
    private static final String SYSTEM_PROMPT = 
            "너는 반려동물이야. 사용자가 보낸 사진을 보고 반려동물의 시점에서, 반려동물의 말투로 짧은 일기를 써줘. " +
            "그리고 사진의 분위기를 보고 기분(mood)을 한 단어로 추측해줘. " +
            "반드시 JSON 형식으로 응답해야 해: {\"content\": \"일기내용\", \"mood\": \"기분\"}";

    @Override
    public AiDiaryResponse generateDiaryContent(String imageUrl) {
        log.info("Requesting AI Diary content for image: {}", imageUrl);

        HttpHeaders headers = createHeaders();
        Map<String, Object> body = createBody(imageUrl);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String response = restTemplate.postForObject(OPENAI_API_URL, request, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new RuntimeException("AI 일기 생성 중 오류가 발생했습니다.", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        return headers;
    }

    private Map<String, Object> createBody(String imageUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o"); // Vision 지원 모델
        body.put("max_tokens", 500);

        // JSON 모드 강제 (응답 파싱을 쉽게 하기 위함)
        Map<String, String> jsonFormat = new HashMap<>();
        jsonFormat.put("type", "json_object");
        body.put("response_format", jsonFormat);

        // 메시지 구성
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // 텍스트 프롬프트 + 이미지 URL
        List<Map<String, Object>> contentList = List.of(
                Map.of("type", "text", "text", SYSTEM_PROMPT),
                Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
        );
        userMessage.put("content", contentList);

        body.put("messages", Collections.singletonList(userMessage));
        return body;
    }

    private AiDiaryResponse parseResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // OpenAI 응답 구조: choices[0].message.content
            String contentString = rootNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            // contentString 자체가 JSON 문자열이므로 다시 파싱하여 DTO로 변환
            return objectMapper.readValue(contentString, AiDiaryResponse.class);

        } catch (Exception e) {
            log.error("Failed to parse OpenAI response", e);
            throw new RuntimeException("AI 응답을 처리하는 데 실패했습니다.", e);
        }
    }
}