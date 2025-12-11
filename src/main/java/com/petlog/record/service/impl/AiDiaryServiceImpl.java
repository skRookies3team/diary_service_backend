package com.petlog.record.service.impl;

import com.petlog.record.dto.response.AiDiaryResponse; // 사용 중인 DTO 이름 확인 필요 (AiDiaryResponse 또는 AiDiaryResponseDto)
import com.petlog.record.entity.Diary;
import com.petlog.record.entity.DiaryImage;
import com.petlog.record.entity.ImageSource; // Enum import 필수
import com.petlog.record.entity.Visibility;
import com.petlog.record.repository.DiaryRepository;
import com.petlog.record.service.AiDiaryService;
import com.petlog.record.service.OpenAiService;
// import com.petlog.record.service.file.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiDiaryServiceImpl implements AiDiaryService {

    private final DiaryRepository diaryRepository;
    private final OpenAiService openAiService;
    // private final S3Uploader s3Uploader;

    @Override
    public Diary createAiDiary(Long userId, Long petId, MultipartFile imageFile, Visibility visibility) {
        log.info("AI Diary creation started for user: {}, pet: {}", userId, petId);

        // 1. [임시] S3 업로드 대체 로직 (Base64)
        String base64Image = toBase64(imageFile);
        String tempDbUrl = "https://temporary-url/test-image.jpg";

        log.debug("Image converted to Base64 for AI test.");

        // 2. AI 서비스 호출
        String aiRequestImage = "data:image/jpeg;base64," + base64Image;
        var aiResponse = openAiService.generateDiaryContent(aiRequestImage);
        log.debug("AI Response received - Mood: {}", aiResponse.getMood());

        // 3. 엔티티 생성
        Diary diary = Diary.builder()
                .userId(userId)
                .petId(petId)
                .content(aiResponse.getContent())
                .mood(aiResponse.getMood())
                .isAiGen(true)
                .visibility(visibility)
                .build();

        // 4. 이미지 연관관계 설정 [수정됨: 필수값 세팅]
        DiaryImage diaryImage = DiaryImage.builder()
                .imageUrl(tempDbUrl)
                .userId(userId)          // [필수] 유저 ID (엔티티에 있다면)
                .imgOrder(1)             // [필수] 이미지 순서 (1번)
                .mainImage(true)         // [필수] 대표 이미지 여부 (True)
                .source(ImageSource.GALLERY) // [필수] 이미지 출처 (임시로 GALLERY 설정)
                .build();

        diary.addImage(diaryImage);

        // 5. DB 저장
        return diaryRepository.save(diary);
    }

    private String toBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("이미지 변환 실패", e);
        }
    }
}