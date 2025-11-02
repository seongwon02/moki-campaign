package com.example.moki_campaign.domain.store.service;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.infra.moki.dto.MokiSalesResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiUserListResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final MokiClient mokiClient;
    private final Executor executor;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 생성자를 직접 작성하여 @Qualifier 명시
    public StoreServiceImpl(MokiClient mokiClient, 
                           @Qualifier("mokiApiExecutor") Executor executor) {
        this.mokiClient = mokiClient;
        this.executor = executor;
    }

    @Override
    public WeeklySummaryResponseDto findWeeklySummary(Store store) {
        LocalDate today = LocalDate.now();
        
        // 이번주 기간 계산 (월요일 ~ 오늘)
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = today;
        
        // 저번주 동일 기간 계산 (저번주 월요일 ~ 저번주 같은 요일)
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);
        
        // 저저번주 기간 계산
        LocalDate weekBeforeLastStart = lastWeekStart.minusWeeks(1);
        LocalDate weekBeforeLastEnd = lastWeekEnd.minusWeeks(1);
        
        String businessNumber = store.getBusinessNumber();
        
        // 비동기로 모든 API 호출 시작
        CompletableFuture<Optional<MokiSalesResponseDto>> thisWeekSalesFuture = 
                CompletableFuture.supplyAsync(() -> mokiClient.getSalesData(
                        businessNumber,
                        thisWeekStart.format(DATE_FORMATTER),
                        thisWeekEnd.format(DATE_FORMATTER)
                ), executor);
        
        CompletableFuture<Optional<MokiUserListResponseDto>> thisWeekUsersFuture = 
                CompletableFuture.supplyAsync(() -> mokiClient.getUserList(
                        businessNumber,
                        thisWeekStart.format(DATE_FORMATTER),
                        thisWeekEnd.format(DATE_FORMATTER)
                ), executor);
        
        CompletableFuture<Optional<MokiSalesResponseDto>> lastWeekSalesFuture = 
                CompletableFuture.supplyAsync(() -> mokiClient.getSalesData(
                        businessNumber,
                        lastWeekStart.format(DATE_FORMATTER),
                        lastWeekEnd.format(DATE_FORMATTER)
                ), executor);
        
        CompletableFuture<Optional<MokiUserListResponseDto>> lastWeekUsersFuture = 
                CompletableFuture.supplyAsync(() -> mokiClient.getUserList(
                        businessNumber,
                        lastWeekStart.format(DATE_FORMATTER),
                        lastWeekEnd.format(DATE_FORMATTER)
                ), executor);
        
        CompletableFuture<Optional<MokiUserListResponseDto>> weekBeforeLastUsersFuture = 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return mokiClient.getUserList(
                                businessNumber,
                                weekBeforeLastStart.format(DATE_FORMATTER),
                                weekBeforeLastEnd.format(DATE_FORMATTER)
                        );
                    } catch (Exception e) {
                        log.warn("저저번주 데이터 조회 실패, 재방문율 변화량을 0으로 계산합니다.", e);
                        return Optional.empty();
                    }
                }, executor);
        
        // 모든 비동기 작업이 완료될 때까지 대기
        CompletableFuture.allOf(
                thisWeekSalesFuture,
                thisWeekUsersFuture,
                lastWeekSalesFuture,
                lastWeekUsersFuture,
                weekBeforeLastUsersFuture
        ).join();
        
        // 결과 가져오기
        Optional<MokiSalesResponseDto> thisWeekSalesOpt = thisWeekSalesFuture.join();
        Optional<MokiUserListResponseDto> thisWeekUsersOpt = thisWeekUsersFuture.join();
        Optional<MokiSalesResponseDto> lastWeekSalesOpt = lastWeekSalesFuture.join();
        Optional<MokiUserListResponseDto> lastWeekUsersOpt = lastWeekUsersFuture.join();
        Optional<MokiUserListResponseDto> weekBeforeLastUsersOpt = weekBeforeLastUsersFuture.join();
        
        // 매출 계산
        long thisWeekTotalSales = thisWeekSalesOpt
                .map(MokiSalesResponseDto::totalRevenue)
                .orElse(0L);
        
        long lastWeekTotalSales = lastWeekSalesOpt
                .map(MokiSalesResponseDto::totalRevenue)
                .orElse(0L);
        
        long salesChange = thisWeekTotalSales - lastWeekTotalSales;
        
        // 방문 고객 수 계산
        int thisWeekVisitorCount = thisWeekUsersOpt
                .map(this::getUniqueVisitorCount)
                .orElse(0);
        
        int lastWeekVisitorCount = lastWeekUsersOpt
                .map(this::getUniqueVisitorCount)
                .orElse(0);
        
        int customerCountChange = thisWeekVisitorCount - lastWeekVisitorCount;
        
        // 재방문율 계산
        double thisWeekRevisitRate = calculateRevisitRate(thisWeekUsersOpt, lastWeekUsersOpt);
        double lastWeekRevisitRate = calculateRevisitRate(lastWeekUsersOpt, weekBeforeLastUsersOpt);
        double revisitRateChange = thisWeekRevisitRate - lastWeekRevisitRate;
        
        return WeeklySummaryResponseDto.builder()
                .startDate(thisWeekStart.format(DATE_FORMATTER))
                .endDate(thisWeekEnd.format(DATE_FORMATTER))
                .totalSales(thisWeekTotalSales)
                .salesChange(salesChange)
                .visitedCustomerCount(thisWeekVisitorCount)
                .customerCountChange(customerCountChange)
                .revisitRate(Math.round(thisWeekRevisitRate * 100.0) / 100.0)
                .revisitRateChange(Math.round(revisitRateChange * 100.0) / 100.0)
                .build();
    }
    
    /**
     * 고유 방문자 수 계산
     */
    private int getUniqueVisitorCount(MokiUserListResponseDto userList) {
        if (userList == null || userList.data() == null) {
            return 0;
        }
        
        Set<String> uniquePhoneNumbers = new HashSet<>();
        for (MokiUserListResponseDto.UserData user : userList.data()) {
            if (user.phoneNum() != null && !user.phoneNum().isEmpty()) {
                uniquePhoneNumbers.add(user.phoneNum());
            }
        }
        
        return uniquePhoneNumbers.size();
    }
    
    /**
     * 재방문율 계산
     * 이번주 방문자 중 저번주에도 방문한 사람의 비율
     */
    private double calculateRevisitRate(Optional<MokiUserListResponseDto> currentWeekUsersOpt, 
                                       Optional<MokiUserListResponseDto> previousWeekUsersOpt) {
        // 이번주 방문자가 없으면 0.0 반환
        if (currentWeekUsersOpt.isEmpty()) {
            return 0.0;
        }
        
        MokiUserListResponseDto currentWeekUsers = currentWeekUsersOpt.get();
        if (currentWeekUsers.data() == null || currentWeekUsers.data().isEmpty()) {
            return 0.0;
        }
        
        // 저번주 방문자 전화번호 Set
        Set<String> previousVisitors = previousWeekUsersOpt
                .map(users -> {
                    Set<String> phoneSet = new HashSet<>();
                    if (users.data() != null) {
                        for (MokiUserListResponseDto.UserData user : users.data()) {
                            if (user.phoneNum() != null && !user.phoneNum().isEmpty()) {
                                phoneSet.add(user.phoneNum());
                            }
                        }
                    }
                    return phoneSet;
                })
                .orElse(new HashSet<>());
        
        // 이번주 방문자 중 재방문자 수 계산
        Set<String> currentVisitors = new HashSet<>();
        int revisitCount = 0;
        
        for (MokiUserListResponseDto.UserData user : currentWeekUsers.data()) {
            if (user.phoneNum() != null && !user.phoneNum().isEmpty()) {
                String phoneNum = user.phoneNum();
                
                // 이번주 방문자 중복 제거
                if (!currentVisitors.contains(phoneNum)) {
                    currentVisitors.add(phoneNum);
                    
                    // 저번주에도 방문했는지 확인
                    if (previousVisitors.contains(phoneNum)) {
                        revisitCount++;
                    }
                }
            }
        }
        
        int totalCurrentVisitors = currentVisitors.size();
        
        if (totalCurrentVisitors == 0) {
            return 0.0;
        }
        
        return (double) revisitCount / totalCurrentVisitors;
    }
}
