package com.petlog.record.service.impl;

import com.petlog.record.client.PetServiceClient;
import com.petlog.record.client.StorageServiceClient;
import com.petlog.record.client.UserServiceClient;
import com.petlog.record.dto.request.DiaryRequest;
import com.petlog.record.dto.response.DiaryResponse;
import com.petlog.record.entity.Diary;
import com.petlog.record.entity.DiaryImage;
import com.petlog.record.entity.ImageSource;
import com.petlog.record.exception.EntityNotFoundException;
import com.petlog.record.exception.ErrorCode;
import com.petlog.record.repository.DiaryRepository;
import com.petlog.record.service.DiaryService;
import feign.FeignException; // [추가] Feign 예외 처리
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;

    private final UserServiceClient userClient;
    private final PetServiceClient petClient;

    @Qualifier("mockStorageServiceClient")
    private final StorageServiceClient storageServiceClient;

    @Override
    @Transactional
    public Long createDiary(DiaryRequest.Create request) {

        // 1. [검증] MSA 회원 서비스 연동 (우회 방식 적용)
        // 회원 서비스에 'exists' API가 없으므로, 'getInfo' API를 호출하여 예외 발생 여부로 존재성을 확인합니다.

        // [1-1] 사용자 존재 여부 확인
        try {
            // 상세 정보를 조회해보고, 성공하면 유저가 존재하는 것으로 간주
            userClient.getUserInfo(request.getUserId());
            log.info("회원 서비스 연동 성공: 유저 확인됨 (userId: {})", request.getUserId()); // [로그 추가]
        } catch (FeignException e) {
            // 404(Not Found)나 500 등의 에러가 발생하면 유저가 없거나 조회 불가능한 상태로 판단
            log.warn("User validation failed for userId: {}. Cause: {}", request.getUserId(), e.getMessage());
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        // [1-2] 펫 존재 여부 확인
        try {
            petClient.getPetInfo(request.getPetId());
            log.info("회원 서비스 연동 성공: 펫 확인됨 (petId: {})", request.getPetId()); // [로그 추가]
        } catch (FeignException e) {
            log.warn("Pet validation failed for petId: {}. Cause: {}", request.getPetId(), e.getMessage());
            throw new EntityNotFoundException(ErrorCode.PET_NOT_FOUND);
        }

        // 2. DTO -> Entity 변환
        Diary diary = request.toEntity();

        // 3. 일기 저장
        Diary savedDiary = diaryRepository.save(diary);

        // 4. 사진 보관함 처리 로직
        processDiaryImages(diary);

        return savedDiary.getDiaryId();
    }

    /**
     * 다이어리에 포함된 이미지 중 '갤러리'에서 가져온 사진을 선별하여
     * 외부 스토리지 서비스(보관함)에 저장 요청을 보냅니다.
     */
    private void processDiaryImages(Diary diary) {
        List<DiaryImage> images = diary.getImages();

        if (images == null || images.isEmpty()) {
            return;
        }

        // 4-1. 전송할 사진 선별 (GALLERY 출처만)
        List<StorageServiceClient.PhotoRequest> newPhotos = images.stream()
                .filter(img -> img.getSource() == ImageSource.GALLERY)
                .map(img -> new StorageServiceClient.PhotoRequest(
                        img.getUserId(),
                        img.getImageUrl()
                ))
                .collect(Collectors.toList());

        // 4-2. 외부 서비스 전송
        if (!newPhotos.isEmpty()) {
            try {
                storageServiceClient.savePhotos(newPhotos);
                log.info("Storage Service: Transferred {} photos for Diary ID {}", newPhotos.size(), diary.getDiaryId());
            } catch (Exception e) {
                // 핵심 비즈니스(일기 저장)가 아니므로, 보관함 전송 실패 시 로그만 남기고 진행
                log.warn("Storage Service Transfer Failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public DiaryResponse getDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponse.fromEntity(diary);
    }

    @Override
    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest.Update request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DIARY_NOT_FOUND));

        // Dirty Checking을 이용한 업데이트
        diary.update(
                request.getContent() != null ? request.getContent() : diary.getContent(),
                request.getVisibility() != null ? request.getVisibility() : diary.getVisibility(),
                request.getWeather() != null ? request.getWeather() : diary.getWeather(),
                request.getMood() != null ? request.getMood() : diary.getMood()
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