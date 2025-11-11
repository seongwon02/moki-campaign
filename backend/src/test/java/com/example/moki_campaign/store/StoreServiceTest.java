package com.example.moki_campaign.store;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.service.StoreServiceImpl;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreServiceImpl storeService;

    @Mock
    private DailyVisitRepository dailyVisitRepository;

    private Store testStore;

    @BeforeEach
    void setUp() {
        testStore = Store.builder()
                .businessNumber("1234567890")
                .password("encodedPassword")
                .name("테스트 매장")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Test
    void 정상적인_주간_요약_조회() {
        // given
        given(dailyVisitRepository.getSalesByDateRange(eq(testStore), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(10000L, 8000L); // thisWeek, lastWeek
        given(dailyVisitRepository.getVisitorCountByDateRange(eq(testStore), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(120, 100); // thisWeek, lastWeek

        given(dailyVisitRepository.getVisitorIdsByDateRange(eq(testStore), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(
                        Set.of(1L, 2L, 3L, 4L),
                        Set.of(2L, 3L, 5L, 6L),
                        Set.of(10L, 11L, 12L),
                        Set.of(11L, 12L, 13L)
                );

        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(result.getEndDate()).matches("\\d{4}-\\d{2}-\\d{2}");

        // 매출 검증
        assertThat(result.getTotalSales()).isEqualTo(10000L);
        assertThat(result.getSalesChange()).isEqualTo(2000L); // 10000 - 8000

        // 방문 수 검증
        assertThat(result.getVisitedCustomerCount()).isEqualTo(120);
        assertThat(result.getCustomerCountChange()).isEqualTo(20); // 120 - 100

        // 재방문율 검증
        assertThat(result.getRevisitRate()).isEqualTo(0.5);
        assertThat(result.getRevisitRateChange()).isCloseTo(-0.1666, within(0.0001));
    }
}
