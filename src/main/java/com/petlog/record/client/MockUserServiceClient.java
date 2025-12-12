package com.petlog.record.client;

import com.petlog.record.dto.client.UserClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary // [중요] 실제 FeignClient 대신 이 빈을 우선적으로 주입함
@Profile({"test", "local-test"}) // 로컬 테스트 환경에서만 활성화
public class MockUserServiceClient implements UserServiceClient {

    @Override
    public Boolean checkUserExists(Long userId) {
        log.info("[Mock] UserServiceClient: 사용자 존재 여부 확인 요청 (userId={}) -> true 반환", userId);
        // 테스트를 위해 무조건 true 반환 (실제 서버 통신 X)
        return true;
    }

    @Override
    public UserClientResponse getUserInfo(Long userId) {
        log.info("[Mock] UserServiceClient: 사용자 정보 조회 요청 (userId={})", userId);
        return UserClientResponse.builder()
                .username("테스트유저")
                .genderType("MALE")
                .age(25)
                .profileImage("https://mock-url.com/profile.jpg")
                .statusMessage("테스트 중입니다.")
                .build();
    }
}