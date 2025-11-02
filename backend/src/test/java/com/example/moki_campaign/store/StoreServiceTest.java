package com.example.moki_campaign.store;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.service.StoreServiceImpl;
import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.infra.moki.dto.MokiSalesResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiUserListResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private MokiClient mokiClient;

    @Mock
    private Executor executor;

    @InjectMocks
    private StoreServiceImpl storeService;

    private Store testStore;

    @BeforeEach
    void setUp() {
        testStore = Store.builder()
                .businessNumber("1234567890")
                .password("encodedPassword")
                .name("테스트 매장")
                .phoneNumber("010-1234-5678")
                .build();

        // Executor를 동기적으로 실행하도록 설정 (테스트 환경)
        willAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).given(executor).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("주간 요약 조회 성공 - 정상 데이터")
    void findWeeklySummary_Success() {
        // given
        MokiSalesResponseDto thisWeekSales = new MokiSalesResponseDto(3500000L, 150, List.of());
        MokiSalesResponseDto lastWeekSales = new MokiSalesResponseDto(3300000L, 140, List.of());

        MokiUserListResponseDto.UserData user1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData user2 = new MokiUserListResponseDto.UserData(
                "김철수", "010-2222-2222", "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(List.of(user1, user2));

        MokiUserListResponseDto.UserData user3 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "4000", "2", 8, LocalDateTime.now().minusWeeks(1)
        );
        MokiUserListResponseDto lastWeekUsers = new MokiUserListResponseDto(List.of(user3));

        MokiUserListResponseDto weekBeforeLastUsers = new MokiUserListResponseDto(List.of());

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekSales))
                .willReturn(Optional.of(lastWeekSales));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.of(lastWeekUsers))
                .willReturn(Optional.of(weekBeforeLastUsers));

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSales()).isEqualTo(3500000L);
        assertThat(result.getSalesChange()).isEqualTo(200000L); // 3500000 - 3300000
        assertThat(result.getVisitedCustomerCount()).isEqualTo(2); // 홍길동, 김철수
        assertThat(result.getCustomerCountChange()).isEqualTo(1); // 2 - 1
        assertThat(result.getRevisitRate()).isEqualTo(0.5); // 1/2 (홍길동만 재방문)
    }

    @Test
    @DisplayName("주간 요약 조회 - 매출 데이터 없을 때")
    void findWeeklySummary_NoSalesData() {
        // given
        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSales()).isEqualTo(0L);
        assertThat(result.getSalesChange()).isEqualTo(0L);
        assertThat(result.getVisitedCustomerCount()).isEqualTo(0);
        assertThat(result.getCustomerCountChange()).isEqualTo(0);
        assertThat(result.getRevisitRate()).isEqualTo(0.0);
        assertThat(result.getRevisitRateChange()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("주간 요약 조회 - 이번주만 데이터 있을 때")
    void findWeeklySummary_OnlyThisWeekData() {
        // given
        MokiSalesResponseDto thisWeekSales = new MokiSalesResponseDto(2000000L, 80, List.of());
        MokiUserListResponseDto.UserData user1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(List.of(user1));

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekSales))
                .willReturn(Optional.empty());

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.empty())
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getTotalSales()).isEqualTo(2000000L);
        assertThat(result.getSalesChange()).isEqualTo(2000000L); // 2000000 - 0
        assertThat(result.getVisitedCustomerCount()).isEqualTo(1);
        assertThat(result.getCustomerCountChange()).isEqualTo(1);
        assertThat(result.getRevisitRate()).isEqualTo(0.0); // 저번주 데이터 없음
    }

    @Test
    @DisplayName("재방문율 계산 - 모든 방문자가 재방문")
    void calculateRevisitRate_AllRevisitors() {
        // given
        MokiSalesResponseDto salesData = new MokiSalesResponseDto(1000000L, 50, List.of());

        MokiUserListResponseDto.UserData user1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData user2 = new MokiUserListResponseDto.UserData(
                "김철수", "010-2222-2222", "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(List.of(user1, user2));

        // 저번주에도 같은 사람들이 방문
        MokiUserListResponseDto lastWeekUsers = new MokiUserListResponseDto(List.of(user1, user2));

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(salesData));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.of(lastWeekUsers))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getRevisitRate()).isEqualTo(1.0); // 2/2 = 100%
    }

    @Test
    @DisplayName("재방문율 계산 - 재방문자 없음")
    void calculateRevisitRate_NoRevisitors() {
        // given
        MokiSalesResponseDto salesData = new MokiSalesResponseDto(1000000L, 50, List.of());

        MokiUserListResponseDto.UserData user1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData user2 = new MokiUserListResponseDto.UserData(
                "김철수", "010-2222-2222", "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(List.of(user1, user2));

        MokiUserListResponseDto.UserData user3 = new MokiUserListResponseDto.UserData(
                "이영희", "010-3333-3333", "4000", "2", 8, LocalDateTime.now().minusWeeks(1)
        );
        MokiUserListResponseDto lastWeekUsers = new MokiUserListResponseDto(List.of(user3));

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(salesData));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.of(lastWeekUsers))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getRevisitRate()).isEqualTo(0.0); // 0/2 = 0%
    }

    @Test
    @DisplayName("고유 방문자 수 계산 - 중복 전화번호 제거")
    void uniqueVisitorCount_RemoveDuplicates() {
        // given
        MokiSalesResponseDto salesData = new MokiSalesResponseDto(1000000L, 50, List.of());

        // 같은 전화번호로 여러 번 방문
        MokiUserListResponseDto.UserData visit1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "2000", "1", 3, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData visit2 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData visit3 = new MokiUserListResponseDto.UserData(
                "김철수", "010-2222-2222", "5000", "3", 8, LocalDateTime.now()
        );

        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(
                List.of(visit1, visit2, visit3)
        );

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(salesData));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.empty())
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getVisitedCustomerCount()).isEqualTo(2); // 홍길동(중복 제거), 김철수
    }

    @Test
    @DisplayName("매출 변화 계산 - 증가")
    void salesChange_Increase() {
        // given
        MokiSalesResponseDto thisWeekSales = new MokiSalesResponseDto(5000000L, 200, List.of());
        MokiSalesResponseDto lastWeekSales = new MokiSalesResponseDto(4500000L, 180, List.of());

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekSales))
                .willReturn(Optional.of(lastWeekSales));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getSalesChange()).isEqualTo(500000L); // 증가
        assertThat(result.getSalesChange()).isPositive();
    }

    @Test
    @DisplayName("매출 변화 계산 - 감소")
    void salesChange_Decrease() {
        // given
        MokiSalesResponseDto thisWeekSales = new MokiSalesResponseDto(3000000L, 120, List.of());
        MokiSalesResponseDto lastWeekSales = new MokiSalesResponseDto(3500000L, 150, List.of());

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekSales))
                .willReturn(Optional.of(lastWeekSales));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getSalesChange()).isEqualTo(-500000L); // 감소
        assertThat(result.getSalesChange()).isNegative();
    }

    @Test
    @DisplayName("날짜 형식 검증")
    void dateFormat_Validation() {
        // given
        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getStartDate()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(result.getEndDate()).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    @DisplayName("소수점 반올림 검증 - 재방문율")
    void revisitRate_RoundingValidation() {
        // given
        MokiSalesResponseDto salesData = new MokiSalesResponseDto(1000000L, 50, List.of());

        // 3명 중 1명 재방문 = 0.333...
        MokiUserListResponseDto.UserData user1 = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData user2 = new MokiUserListResponseDto.UserData(
                "김철수", "010-2222-2222", "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData user3 = new MokiUserListResponseDto.UserData(
                "이영희", "010-3333-3333", "2000", "1", 3, LocalDateTime.now()
        );
        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(
                List.of(user1, user2, user3)
        );

        MokiUserListResponseDto lastWeekUsers = new MokiUserListResponseDto(List.of(user1));

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(salesData));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.of(lastWeekUsers))
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getRevisitRate()).isEqualTo(0.33); // 소수점 2자리 반올림
    }

    @Test
    @DisplayName("Null 전화번호 처리")
    void handleNullPhoneNumber() {
        // given
        MokiSalesResponseDto salesData = new MokiSalesResponseDto(1000000L, 50, List.of());

        MokiUserListResponseDto.UserData validUser = new MokiUserListResponseDto.UserData(
                "홍길동", "010-1111-1111", "5000", "3", 10, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData nullPhoneUser = new MokiUserListResponseDto.UserData(
                "김철수", null, "3000", "2", 5, LocalDateTime.now()
        );
        MokiUserListResponseDto.UserData emptyPhoneUser = new MokiUserListResponseDto.UserData(
                "이영희", "", "2000", "1", 3, LocalDateTime.now()
        );

        MokiUserListResponseDto thisWeekUsers = new MokiUserListResponseDto(
                List.of(validUser, nullPhoneUser, emptyPhoneUser)
        );

        given(mokiClient.getSalesData(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(salesData));

        given(mokiClient.getUserList(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(thisWeekUsers))
                .willReturn(Optional.empty())
                .willReturn(Optional.empty());

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result.getVisitedCustomerCount()).isEqualTo(1); // null, empty 제외하고 1명만
    }
}
