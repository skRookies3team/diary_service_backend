package com.petlog.record.client;

import com.petlog.record.dto.client.PetClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Primary // [중요] 실제 FeignClient 대신 이 빈을 우선적으로 주입함
@Profile({"test", "local-test"}) // 로컬 테스트 환경에서만 활성화
public class MockPetServiceClient implements PetServiceClient {

    @Override
    public Boolean checkPetExists(Long petId) {
        log.info("[Mock] PetServiceClient: 펫 존재 여부 확인 요청 (petId={}) -> true 반환", petId);
        // 테스트를 위해 무조건 true 반환 (실제 서버 통신 X)
        return true;
    }

    @Override
    public PetClientResponse getPetInfo(Long petId) {
        log.info("[Mock] PetServiceClient: 펫 정보 조회 요청 (petId={})", petId);
        return PetClientResponse.builder()
                .petId(petId)
                .petName("바둑이")
                .breed("말티즈")
                .age(3)
                .genderType("MALE")
                .isNeutered(true)
                .status("ACTIVE")
                .profileImage("https://mock-url.com/pet.jpg")
                .birth(LocalDateTime.now())
                .species("DOG")
                .build();
    }
}