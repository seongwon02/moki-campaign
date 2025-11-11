package com.example.moki_campaign.global.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 날짜 범위 계산 유틸리티
 */
public class DateRangeCalculator {
    
    /**
     * 직전 6개월 완료된 데이터 범위 계산
     * 매달 1일에 실행되며, 전월까지의 완료된 6개월 데이터를 분석
     * 
     * 예시:
     * - 2025-11-01 실행 → 2025-05-01 ~ 2025-10-31 (5월~10월)
     * - 2025-12-01 실행 → 2025-06-01 ~ 2025-11-30 (6월~11월)
     * 
     * @param baseDate 기준 날짜 (보통 오늘, 매달 1일)
     * @return 시작일과 종료일을 포함하는 DateRange
     */
    public static DateRange getLastSixMonthsRange(LocalDate baseDate) {
        LocalDate endDate = baseDate.minusMonths(1)
                                    .withDayOfMonth(1)
                                    .plusMonths(1)
                                    .minusDays(1);
        
        LocalDate startDate = endDate.minusMonths(5)
                                     .withDayOfMonth(1);
        
        return new DateRange(startDate, endDate);
    }
    

    // 날짜 범위를 나타내는 레코드
    public record DateRange(LocalDate startDate, LocalDate endDate) {
        
        public long getDays() {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        
        public long getMonths() {
            return ChronoUnit.MONTHS.between(
                    startDate.withDayOfMonth(1), 
                    endDate.withDayOfMonth(1)
            ) + 1;
        }
        
        public String getMonthRangeKorean() {
            return String.format("%d월~%d월", 
                    startDate.getMonthValue(), 
                    endDate.getMonthValue());
        }
        
        @Override
        public String toString() {
            return String.format("%s ~ %s (%d일간, %s)", 
                    startDate, endDate, getDays(), getMonthRangeKorean());
        }
    }
}
