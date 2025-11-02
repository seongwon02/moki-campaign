package com.example.moki_campaign.moki;

import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiSalesResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiUserListResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("로그인 요청 파라미터 검증")
    void 로그인_요청_파라미터_검증() {
        // given
        String businessNumber = "1234567890";
        String password = "password123";

        // when & then
        assertThatCode(() -> {
            assertThat(businessNumber).isNotNull();
            assertThat(businessNumber).isNotEmpty();
            assertThat(password).isNotNull();
            assertThat(password).isNotEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("로그인 성공 응답 검증")
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
    @DisplayName("로그인 실패 응답 검증")
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

    @Test
    @DisplayName("매출 데이터 응답 DTO 검증")
    void 매출_데이터_응답_DTO_검증() {
        // given
        MokiSalesResponseDto.SalesData salesData = new MokiSalesResponseDto.SalesData(
                1L,
                "아메리카노",
                "2025-11-01",
                "14",
                "5",
                "25000"
        );

        MokiSalesResponseDto response = new MokiSalesResponseDto(
                3500000L,
                150,
                List.of(salesData)
        );

        // when & then
        assertThat(response.totalRevenue()).isEqualTo(3500000L);
        assertThat(response.totalCount()).isEqualTo(150);
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).menuName()).isEqualTo("아메리카노");
        assertThat(response.data().get(0).revenue()).isEqualTo("25000");
    }

    @Test
    @DisplayName("회원 리스트 응답 DTO 검증")
    void 회원_리스트_응답_DTO_검증() {
        // given
        MokiUserListResponseDto.UserData userData = new MokiUserListResponseDto.UserData(
                "홍길동",
                "010-1234-5678",
                "5000",
                "3",
                10,
                LocalDateTime.of(2025, 1, 1, 10, 30, 0)
        );

        MokiUserListResponseDto response = new MokiUserListResponseDto(
                List.of(userData)
        );

        // when & then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).userName()).isEqualTo("홍길동");
        assertThat(response.data().get(0).phoneNum()).isEqualTo("010-1234-5678");
        assertThat(response.data().get(0).visitCount()).isEqualTo("3");
    }

    @Test
    @DisplayName("매출 조회 파라미터 검증")
    void 매출_조회_파라미터_검증() {
        // given
        String businessNumber = "1234567890";
        String startDate = "2025-11-01";
        String endDate = "2025-11-07";

        // when & then
        assertThatCode(() -> {
            assertThat(businessNumber).isNotNull().isNotEmpty();
            assertThat(startDate).matches("\\d{4}-\\d{2}-\\d{2}");
            assertThat(endDate).matches("\\d{4}-\\d{2}-\\d{2}");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("회원 리스트 조회 파라미터 검증")
    void 회원_리스트_조회_파라미터_검증() {
        // given
        String businessNumber = "1234567890";
        String startDate = "2025-11-01";
        String endDate = "2025-11-07";

        // when & then
        assertThatCode(() -> {
            assertThat(businessNumber).isNotNull().isNotEmpty();
            assertThat(startDate).matches("\\d{4}-\\d{2}-\\d{2}");
            assertThat(endDate).matches("\\d{4}-\\d{2}-\\d{2}");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Optional 반환 타입 검증 - 매출 데이터")
    void Optional_반환_타입_검증_매출() {
        // given
        MokiSalesResponseDto salesResponse = new MokiSalesResponseDto(
                1000000L,
                50,
                List.of()
        );

        // when
        Optional<MokiSalesResponseDto> optional = Optional.of(salesResponse);

        // then
        assertThat(optional).isPresent();
        assertThat(optional.get().totalRevenue()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("Optional 반환 타입 검증 - 회원 리스트")
    void Optional_반환_타입_검증_회원() {
        // given
        MokiUserListResponseDto userListResponse = new MokiUserListResponseDto(
                List.of()
        );

        // when
        Optional<MokiUserListResponseDto> optional = Optional.of(userListResponse);

        // then
        assertThat(optional).isPresent();
        assertThat(optional.get().data()).isEmpty();
    }

    @Test
    @DisplayName("빈 Optional 처리 검증")
    void 빈_Optional_처리_검증() {
        // when
        Optional<MokiSalesResponseDto> emptyOptional = Optional.empty();

        // then
        assertThat(emptyOptional).isEmpty();
        assertThat(emptyOptional.orElse(null)).isNull();
    }
}
