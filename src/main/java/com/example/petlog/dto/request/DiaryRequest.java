package com.example.petlog.dto.request;

import com.example.petlog.entity.ImageSource;
import com.example.petlog.entity.Diary;
import com.example.petlog.entity.DiaryImage;
import com.example.petlog.entity.PhotoArchive;
import com.example.petlog.entity.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DiaryRequest {

    // [Request] 일기 생성
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {

        @NotNull
        private Long userId;
        @NotNull
        private Long petId;

        private String content;
        private Visibility visibility;
        private Boolean isAiGen;
        private String weather;
        private String mood;

        // 이미지 리스트
        private List<Image> images;

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

            if (this.images != null) {
                this.images.stream()
                        .map(Image::toEntity)
                        .forEach(diary::addImage);
            }
            return diary;
        }

        // [수정] PhotoArchive 변환 시 source 값 매핑 확인
        public List<PhotoArchive> toPhotoArchiveEntities() {
            if (this.images == null || this.images.isEmpty()) {
                return Collections.emptyList();
            }
            return this.images.stream()
                    .map(img -> PhotoArchive.builder()
                            .userId(this.userId)
                            .imageUrl(img.getImageUrl())
                            .source(img.getSource()) // 여기서 값을 넣어줍니다.
                            .build())
                    .collect(Collectors.toList());
        }
    }

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String imageUrl;
        private Integer imgOrder;
        private Boolean mainImage;

        // [추가] 이미지 출처 (GALLERY, ARCHIVE)
        // 클라이언트가 "이 사진 갤러리에서 가져왔어요/보관함에서 골랐어요"라고 알려줘야 함
        private ImageSource source;

        public DiaryImage toEntity() {
            return DiaryImage.builder()
                    .imageUrl(this.imageUrl)
                    .imgOrder(this.imgOrder)
                    .mainImage(this.mainImage)
                    .build();
        }
    }
}