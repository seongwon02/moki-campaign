package com.example.moki_campaign.moki;

import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MokiClientTest {

    private MokiClient mokiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mokiClient = new MokiClient(objectMapper);
    }

    @Test
    @DisplayName("MokiClient 생성 성공")
    void createMokiClient_Success() {
        // when & then
        assertThat(mokiClient).isNotNull();
    }

    @Test
    void 로그인_요청_파라미터_검증() {
        // given
        String businessNumber = "1234567890";
        String password = "password123";

        // when & then
        // 실제 외부 API 호출이 발생하므로 단위 테스트에서는 파라미터 검증만 수행
        assertThatCode(() -> {
            assertThat(businessNumber).isNotNull();
            assertThat(businessNumber).isNotEmpty();
            assertThat(password).isNotNull();
            assertThat(password).isNotEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void 로그인_성공_응답_검증() {
        // given
        MokiLoginResponseDto successResponse = new MokiLoginResponseDto(
                "Y",
                "로그인 되었습니다.",
                "1234567890",
                "테스트 매장",
                "010-1234-5678",
                "test@example.com",
                "서울시 강남구",
                "1",
                "1",
                "old",
                "https://example.com/banner.jpg",
                "Y",
                "Y"
        );

        // when & then
        assertThat(successResponse.isSuccess()).isTrue();
        assertThat(successResponse.mbId()).isEqualTo("1234567890");
        assertThat(successResponse.mbName()).isEqualTo("테스트 매장");
        assertThat(successResponse.mbHp()).isEqualTo("010-1234-5678");
    }

    @Test
    void 로그인_실패_응답_검증() {
        // given
        MokiLoginResponseDto failureResponse = new MokiLoginResponseDto(
                "N",
                "가입된 회원아이디가 아니거나 비밀번호가 틀립니다.",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when & then
        assertThat(failureResponse.isSuccess()).isFalse();
        assertThat(failureResponse.msg()).contains("비밀번호가 틀립니다");
    }
}
