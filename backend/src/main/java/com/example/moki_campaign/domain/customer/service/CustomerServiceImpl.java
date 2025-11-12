package com.example.moki_campaign.domain.customer.service;

import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import com.example.moki_campaign.global.util.DateRangeCalculator;
import com.example.moki_campaign.global.util.DateRangeCalculator.DateRange;
import com.example.moki_campaign.infra.ai.client.AiClient;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataInputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataOutputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final DailyVisitRepository dailyVisitRepository;
    private final AiClient aiClient;

    private final CustomerService self;

    public CustomerServiceImpl(
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            DailyVisitRepository dailyVisitRepository,
            AiClient aiClient,
            @Lazy CustomerService self) {
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.dailyVisitRepository = dailyVisitRepository;
        this.aiClient = aiClient;
        this.self = self;
    }
    // 전체 매장의 고객들에 대한 ai 고객 분석
    // 테스트 위해 @Async 추가(테스트 완료하면 테스트 로직 삭제 예정)
    @Async
    @Override
    public void analyzeAllStores() {

        List<Store> stores = storeRepository.findAll();

        if (stores.isEmpty()) {
            log.warn("분석 대상 매장이 없습니다.");
            return;
        }

        log.info("분석 대상 매장 수: {}개", stores.size());

        int successCount = 0;
        int failCount = 0;

        for (Store store : stores) {
            try {
                self.analyzeStore(store);
                successCount++;
            } catch (Exception e) {
                log.error("매장({}) AI 분석 실패", store.getName(), e);
                failCount++;
            }
        }

        log.info("전체 매장 AI 고객 분석 완료 - 성공: {}개, 실패: {}개", successCount, failCount);
    }

    // 특정 매장의 고객을 대상으로 ai 분석
    // 항상 새로운 트렌젝션을 시작
    @Override
    @Transactional
    public void analyzeStore(Store store) {

        try {
            List<AiCustomerDataInputDto> inputData = prepareDataForAnalysis(store);

            if (inputData.isEmpty()) {
                log.warn("매장({}) 분석 대상 데이터가 없습니다.", store.getName());
                return;
            }

            AiCustomerDataResponseDto aiResponse = aiClient.analyzeCustomers(inputData);

            if (aiResponse == null || aiResponse.result() == null || aiResponse.result().isEmpty()) {
                log.error("매장({}) AI 분석 결과가 비어있습니다.", store.getName());
                return;
            }

            int updatedCount = updateCustomersFromAiResult(
                    store,
                    aiResponse.result()
            );

            log.info("매장({}) AI 고객 분석 완료: {}명 업데이트", store.getName(), updatedCount);

        } catch (Exception e) {
            log.error("매장({}) AI 고객 분석 중 예외 발생", store.getName(), e);
            throw e; // 상위로 전파하여 실패 카운트
        }
    }

    // ai 분석을 위한 요청 데이터 생성
    private List<AiCustomerDataInputDto> prepareDataForAnalysis(Store store) {
        DateRange dateRange = DateRangeCalculator.getLastSixMonthsRange(LocalDate.now());
        LocalDate startDate = dateRange.startDate();
        LocalDate endDate = dateRange.endDate();

        List<Customer> customers = customerRepository.findAllByStore(store);

        if (customers.isEmpty()) {
            log.warn("매장({})에 고객 데이터가 없습니다.", store.getName());
            return List.of();
        }

        List<DailyVisit> visits = dailyVisitRepository
                .findByStoreAndDateRangeWithCustomer(store, startDate, endDate);

        log.info("고객 수: {}명, 방문 기록 수: {}건", customers.size(), visits.size());

        Map<Long, List<DailyVisit>> visitsByCustomer = visits.stream()
                .collect(Collectors.groupingBy(v -> v.getCustomer().getId()));

        List<AiCustomerDataInputDto> result = customers.stream()
                .map(customer -> {
                    List<DailyVisit> customerVisits = visitsByCustomer
                            .getOrDefault(customer.getId(), List.of());
                    return convertToAiInputDto(customer, customerVisits, endDate);
                })
                .collect(Collectors.toList());

        log.debug("AI 분석용 데이터 변환 완료: {}건", result.size());

        return result;
    }

    // ai 고객 분석용 dto 생성
    private AiCustomerDataInputDto convertToAiInputDto(
            Customer customer,
            List<DailyVisit> visits,
            LocalDate analysisEndDate) {

        double totalAmount = visits.stream()
                .mapToInt(DailyVisit::getAmount)
                .sum();

        int totalVisits = visits.size();

        int daysSinceLastVisit = visits.stream()
                .map(DailyVisit::getVisitDate)
                .max(LocalDate::compareTo)
                .map(lastVisit -> (int) ChronoUnit.DAYS.between(lastVisit, analysisEndDate))
                .orElse(9999); // 방문 기록이 없으면 매우 큰 값

        YearMonth endMonth = YearMonth.from(analysisEndDate);

        int visits6MonthAgo = countVisitsInMonth(visits, endMonth.minusMonths(5));
        int visits5MonthAgo = countVisitsInMonth(visits, endMonth.minusMonths(4));
        int visits4MonthAgo = countVisitsInMonth(visits, endMonth.minusMonths(3));
        int visits3MonthAgo = countVisitsInMonth(visits, endMonth.minusMonths(2));
        int visits2MonthAgo = countVisitsInMonth(visits, endMonth.minusMonths(1));
        int visits1MonthAgo = countVisitsInMonth(visits, endMonth);

        return AiCustomerDataInputDto.builder()
                .customerId(String.valueOf(customer.getId()))
                .amount(totalAmount)
                .totalVisits(totalVisits)
                .daysSinceLastVisit(daysSinceLastVisit)
                .visits6MonthAgo(visits6MonthAgo)
                .visits5MonthAgo(visits5MonthAgo)
                .visits4MonthAgo(visits4MonthAgo)
                .visits3MonthAgo(visits3MonthAgo)
                .visits2MonthAgo(visits2MonthAgo)
                .visits1MonthAgo(visits1MonthAgo)
                .build();
    }

    // 한달 동안 방문 횟수 계산
    private int countVisitsInMonth(List<DailyVisit> visits, YearMonth targetMonth) {
        return (int) visits.stream()
                .filter(visit -> YearMonth.from(visit.getVisitDate()).equals(targetMonth))
                .count();
    }

    // ai 분석 결과 바탕으로 고객 점보 업데이트
    private int updateCustomersFromAiResult(Store store, List<AiCustomerDataOutputDto> aiResults) {

        if (aiResults.isEmpty()) {
            log.warn("AI 분석 결과가 비어있습니다.");
            return 0;
        }

        Map<Long, AiCustomerDataOutputDto> resultMap = aiResults.stream()
                .collect(Collectors.toMap(
                        result -> parseCustomerId(result.customerId()),
                        result -> result,
                        (existing, replacement) -> replacement
                ));

        List<Customer> customers = customerRepository.findAllByStore(store);

        int updateCount = 0;

        for (Customer customer : customers) {
            AiCustomerDataOutputDto aiResult = resultMap.get(customer.getId());

            if (aiResult != null) {
                CustomerSegment segment = CustomerSegment.fromString(aiResult.customerSegment());

                int loyaltyScore = (int) Math.round(aiResult.predictedLoyaltyScore() * 100);

                customerRepository.updateSegmentAndLoyaltyScore(
                        customer.getId(),
                        segment,
                        loyaltyScore
                );
                updateCount++;

                log.debug("고객(ID: {}) 업데이트: segment={}, loyaltyScore={}",
                        customer.getId(),
                        segment,
                        loyaltyScore);
            } else {
                log.warn("고객(ID: {})에 대한 AI 분석 결과를 찾을 수 없습니다.",
                        customer.getId());
            }
        }

        log.info("매장({}) AI 분석 결과 반영 완료: {}건 업데이트", store.getName(), updateCount);

        return updateCount;
    }

    // 문자열 형태의 고객 id를 Long으로 변환
    private Long parseCustomerId(String customerId) {
        try {
            return Long.parseLong(customerId);
        } catch (NumberFormatException e) {
            log.error("Invalid customer_id format: {}", customerId);
            return -1L;
        }
    }
}