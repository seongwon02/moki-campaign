package com.example.moki_campaign.util;

import com.example.moki_campaign.global.util.DateRangeCalculator;
import com.example.moki_campaign.global.util.DateRangeCalculator.DateRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class DateRangeCalculatorTest {

    @Test
    void 정상적인_6개월_조회_테스트() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 11, 1);
        
        // when
        DateRange result = DateRangeCalculator.getLastSixMonthsRange(baseDate);
        
        // then
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2025, 5, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2025, 10, 31));
        assertThat(result.getMonths()).isEqualTo(6);
        assertThat(result.getMonthRangeKorean()).isEqualTo("5월~10월");
    }
    
    @Test
    void 해를_넘긴_경우_6개월_조회_테스트() {
        // given
        LocalDate baseDate = LocalDate.of(2026, 1, 1);
        
        // when
        DateRange result = DateRangeCalculator.getLastSixMonthsRange(baseDate);
        
        // then
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2025, 7, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(result.getMonths()).isEqualTo(6);
        assertThat(result.getMonthRangeKorean()).isEqualTo("7월~12월");
    }
    
    @Test
    void 윤년인_경우의_6개월_조회_테스트() {
        // given
        LocalDate baseDate = LocalDate.of(2024, 3, 1); // 2024년은 윤년
        
        // when
        DateRange result = DateRangeCalculator.getLastSixMonthsRange(baseDate);
        
        // then
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2023, 9, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2024, 2, 29)); // 윤년
        assertThat(result.getMonths()).isEqualTo(6);
    }
    
    @Test
    void 일수_계산_테스트() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 11, 1);
        
        // when
        DateRange result = DateRangeCalculator.getLastSixMonthsRange(baseDate);
        
        // then
        assertThat(result.getDays()).isEqualTo(184);
    }
}
