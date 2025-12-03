package com.example.petlog.dto.request;

import com.example.petlog.entity.Diary;
import com.example.petlog.entity.DiaryImage;
import com.example.petlog.entity.Visibility; // Visibility Enum 위치 확인 필요
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DiaryRequest {

    // [Request] 일기 생성
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {

        @NotNull(message = "사용자 ID는 필수입니다.")
        private Long userId;

        @NotNull(message = "펫 ID는 필수입니다.")
        private Long petId;

        private String content;
        private Visibility visibility;
        private Boolean isAiGen;
        private String weather;
        private String mood;

        // 이미지 리스트
        private List<Image> images;

        // DTO -> Entity 변환
        public Diary toEntity() {
            Diary diary = Diary.builder()
                    .userId(this.userId)
                    .petId(this.petId)
                    .content(this.content)
                    .visibility(this.visibility)
                    .isAiGen(this.isAiGen)
                    .weather(this.weather)
                    .mood(this.mood)
                    .build();

            // 이미지 리스트가 있다면 변환하여 추가
            if (this.images != null) {
                this.images.stream()
                        .map(Image::toEntity)
                        .forEach(diary::addImage); // Diary 엔티티의 편의 메서드 사용
            }

            return diary;
        }
    }

    // [Request] 일기 수정
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String content;
        private Visibility visibility;
        private String weather;
        private String mood;
    }

    // [Inner DTO] 이미지 요청용
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String imageUrl;
        private Integer imgOrder;
        private Boolean mainImage;

        // DTO -> Entity 변환
        public DiaryImage toEntity() {
            return DiaryImage.builder()
                    .imageUrl(this.imageUrl)
                    .imgOrder(this.imgOrder)
                    .mainImage(this.mainImage)
                    .build();
        }
    }
}