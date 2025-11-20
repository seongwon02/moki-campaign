package com.example.moki_campaign.domain.customer.scheduler;

import com.example.moki_campaign.domain.customer.service.CustomerService; // CustomerAnalysisService -> CustomerService로 변경
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 고객 분석 스케줄러
 * 매주 월요일 자정에 실행되어 직전 6개월 데이터 분석 통한 단골 점수와 segment 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerAnalysisScheduler {

    private final CustomerService customerService;

    /**
     * 매주 월요일 00:00:00에 실행
     * cron 표현식: "초 분 시 일 월 요일"
     * MON: 월요일 (Sunday=SUN, Monday=MON, Tuesday=TUE, ...)
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void scheduleWeeklyAnalysis() {
        log.info("========================================");
        log.info("AI 고객 분석 스케줄 시작 (매주 월요일)");
        log.info("========================================");

        try {
            customerService.analyzeAllStores();
            log.info("AI 고객 분석 스케줄 완료");
        } catch (Exception e) {
            log.error("AI 고객 분석 스케줄 실행 중 오류 발생", e);
        }

        log.info("========================================");
    }
}