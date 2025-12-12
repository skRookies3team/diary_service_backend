package com.petlog.record.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetClientResponse {

    // PetResponse.GetPetDto 구조에 맞춤
    private Long petId;

    private String petName;         // 펫 이름
    private String species;         // 종류 (Enum -> String)
    private String breed;           // 품종
    private String genderType;      // 성별 (Enum -> String)

    @JsonProperty("is_neutered")    // JSON 필드명 매핑 (is_neutered)
    private boolean isNeutered;     // 중성화 여부

    private String profileImage;    // 프로필 사진
    private Integer age;            // 나이
    private LocalDateTime birth;    // 생일
    private String status;          // 상태 (Enum -> String)
}