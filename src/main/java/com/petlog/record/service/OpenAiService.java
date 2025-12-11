package com.petlog.record.service;

import com.petlog.record.dto.response.AiDiaryResponse;

public interface OpenAiService {

    /**
     * 이미지 URL을 받아 AI 모델에게 일기 내용과 감정 분석을 요청합니다.
     * * @param imageUrl 분석할 이미지의 URL (S3 등)
     * @return 일기 내용과 감정(mood)이 담긴 DTO
     */
    AiDiaryResponse generateDiaryContent(String imageUrl);
}