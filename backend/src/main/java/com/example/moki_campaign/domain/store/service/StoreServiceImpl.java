package com.example.moki_campaign.domain.store.service;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DailyVisitRepository dailyVisitRepository;

    @Override
    @Transactional(readOnly = true)
    public WeeklySummaryResponseDto findWeeklySummary(Store store) {
        LocalDate today = LocalDate.now();

        // 이번주
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = today;

        // 저번주
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);

        // 매출
        Long thisWeekSales = dailyVisitRepository.getSalesByDateRange(store, thisWeekStart, thisWeekEnd);
        Long lastWeekSales = dailyVisitRepository.getSalesByDateRange(store, lastWeekStart, lastWeekEnd);
        Long salesChange = thisWeekSales - lastWeekSales;

        // 방문 수
        Integer thisWeekVisitors = dailyVisitRepository.getVisitorCountByDateRange(store, thisWeekStart, thisWeekEnd);
        Integer lastWeekVisitors = dailyVisitRepository.getVisitorCountByDateRange(store, lastWeekStart, lastWeekEnd);
        Integer customerCountChange = thisWeekVisitors - lastWeekVisitors;

        // 재방문
        LocalDate prevWeekFullStart = thisWeekStart.minusWeeks(1);
        LocalDate prevWeekFullEnd = prevWeekFullStart.plusDays(6);

        Set<Long> lastWeekFullVisitors = dailyVisitRepository.getVisitorIdsByDateRange(store, prevWeekFullStart, prevWeekFullEnd);
        Set<Long> thisWeekCurrentVisitors = dailyVisitRepository.getVisitorIdsByDateRange(store, thisWeekStart, thisWeekEnd);

        Set<Long> thisWeekRevisitors = thisWeekCurrentVisitors.stream()
                .filter(lastWeekFullVisitors::contains)
                .collect(Collectors.toSet());

        int thisWeekRevisitRate = 0;
        if (!lastWeekFullVisitors.isEmpty()) {
            thisWeekRevisitRate = (int) Math.round((double) thisWeekRevisitors.size() / lastWeekFullVisitors.size() * 100.0);
        }

        // 재방문율 변화량
        LocalDate prevPrevWeekFullStart = prevWeekFullStart.minusWeeks(1); // 2025-10-27 (월)
        LocalDate prevPrevWeekFullEnd = prevPrevWeekFullStart.plusDays(6); // 2025-11-02 (일)
        Set<Long> prevPrevWeekFullVisitors = dailyVisitRepository.getVisitorIdsByDateRange(store, prevPrevWeekFullStart, prevPrevWeekFullEnd);

        Set<Long> lastWeekCurrentVisitors = dailyVisitRepository.getVisitorIdsByDateRange(store, lastWeekStart, lastWeekEnd);

        Set<Long> lastWeekRevisitors = lastWeekCurrentVisitors.stream()
                .filter(prevPrevWeekFullVisitors::contains)
                .collect(Collectors.toSet());

        int lastWeekRevisitRate = 0;
        if (!prevPrevWeekFullVisitors.isEmpty()) {
            lastWeekRevisitRate = (int) Math.round((double) lastWeekRevisitors.size() / prevPrevWeekFullVisitors.size() * 100.0);
        }

        int revisitRateChange = thisWeekRevisitRate - lastWeekRevisitRate;

        return new WeeklySummaryResponseDto(
                store.getName(),
                thisWeekStart.format(DATE_FORMATTER),
                thisWeekEnd.format(DATE_FORMATTER),
                thisWeekSales,
                salesChange,
                thisWeekVisitors,
                customerCountChange,
                thisWeekRevisitRate,
                revisitRateChange
        );
    }
}