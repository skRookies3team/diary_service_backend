package com.example.petlog.service.impl;

import com.example.petlog.client.PetServiceClient;
import com.example.petlog.client.UserServiceClient;
import com.example.petlog.dto.request.DiaryRequest;
import com.example.petlog.dto.response.DiaryResponse;
import com.example.petlog.entity.Diary;
import com.example.petlog.exception.EntityNotFoundException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.repository.DiaryRepository;
import com.example.petlog.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserServiceClient userClient;
    private final PetServiceClient petClient;

    @Override
    @Transactional
    public Long createDiary(DiaryRequest.Create request) {
        // 1. MSA 검증 (로컬 테스트 시 주석 처리 가능)
        /*
        if (!userClient.checkUserExists(request.getUserId())) {
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (!petClient.checkPetExists(request.getPetId())) {
            throw new EntityNotFoundException(ErrorCode.PET_NOT_FOUND);
        }
        */

        // 2. DTO -> Entity 변환 (DTO 내부 로직 사용)
        Diary diary = request.toEntity();

        // 3. 저장 (Cascade 설정으로 인해 Image도 자동 저장됨)
        return diaryRepository.save(diary).getDiaryId();
    }

    @Override
    public DiaryResponse getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DIARY_NOT_FOUND));

        // Entity -> DTO 변환 (메서드명 변경: from -> fromEntity)
        return DiaryResponse.fromEntity(diary);
    }

    @Override
    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest.Update request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DIARY_NOT_FOUND));

        // Dirty Checking을 통한 업데이트
        diary.update(
                request.getContent(),
                request.getVisibility(),
                request.getWeather(),
                request.getMood()
        );
    }

    @Override
    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DIARY_NOT_FOUND));

        diaryRepository.delete(diary);
    }
}