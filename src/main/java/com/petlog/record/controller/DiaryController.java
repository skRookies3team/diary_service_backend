package com.petlog.record.controller;

import com.petlog.record.dto.request.DiaryRequest;
import com.petlog.record.dto.response.DiaryResponse;
import com.petlog.record.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Diary API", description = "다이어리 CRUD 및 관리 API") // 컨트롤러 설명
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "다이어리 생성", description = "사용자 ID와 펫 ID를 기반으로 새로운 일기를 작성합니다. (회원 서비스 연동 검증 포함)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDiary(@Valid @RequestBody DiaryRequest.Create request) {
        // 1. 서비스 호출 및 ID 반환
        Long diaryId = diaryService.createDiary(request);

        // 2. 응답 메시지 커스텀 (Map 사용)
        Map<String, Object> response = new HashMap<>();
        response.put("diaryId", diaryId);
        response.put("message", "일기가 성공적으로 등록되었습니다.");

        // 3. 201 Created 상태코드 + Location 헤더 + Body 반환
        return ResponseEntity
                .created(URI.create("/api/diaries/" + diaryId))
                .body(response);
    }

    @Operation(summary = "다이어리 상세 조회", description = "다이어리 ID를 통해 일기의 상세 내용을 조회합니다.")
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long diaryId) {
        return ResponseEntity.ok(diaryService.getDiary(diaryId));
    }

    @Operation(summary = "다이어리 수정", description = "기존 일기의 내용(텍스트, 공개범위, 날씨, 기분)을 부분 수정합니다.")
    @PatchMapping("/{diaryId}")
    public ResponseEntity<Void> updateDiary(@PathVariable Long diaryId,
                                            @RequestBody DiaryRequest.Update request) {
        diaryService.updateDiary(diaryId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "다이어리 삭제", description = "특정 일기를 삭제합니다.")
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}