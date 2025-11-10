package com.example.moki_campaign.store;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.service.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

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
    }

    @Test
    @DisplayName("주간 요약 조회 - 기본 응답 구조 검증")
    void findWeeklySummary_BasicStructure() {
        // when
        WeeklySummaryResponseDto result = storeService.findWeeklySummary(testStore);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getStartDate()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(result.getEndDate()).matches("\\d{4}-\\d{2}-\\d{2}");
        
        // TODO: 실제 구현 후 데이터 검증 추가
        assertThat(result.getTotalSales()).isEqualTo(0L);
        assertThat(result.getSalesChange()).isEqualTo(0L);
        assertThat(result.getVisitedCustomerCount()).isEqualTo(0);
        assertThat(result.getCustomerCountChange()).isEqualTo(0);
        assertThat(result.getRevisitRate()).isEqualTo(0.0);
        assertThat(result.getRevisitRateChange()).isEqualTo(0.0);
    }
}
