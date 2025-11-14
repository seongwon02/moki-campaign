package com.example.moki_campaign.domain.customer.service;

import com.example.moki_campaign.domain.customer.dto.response.AnalyticsReponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerSummaryDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.global.util.DateRangeCalculator;
import com.example.moki_campaign.global.util.DateRangeCalculator.DateRange;
import com.example.moki_campaign.infra.ai.client.AiClient;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataInputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataOutputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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

    // 이탈 위험 단골 고객 정보 조회
    @Override
    @Transactional(readOnly = true)
    public DeclinedLoyalSummaryResponseDto findDeclinedLoyalInfo(Store store) {

        long atRiskLoyalCount = customerRepository.countByStoreAndSegment(store, CustomerSegment.AT_RISK_LOYAL);

        List<CustomerSegment> loyalSegments = List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL);
        long totalLoyalCount = customerRepository.countByStoreAndSegmentIn(store, loyalSegments);

        // 비율 계산
        int declineRatio = 0;
        if (totalLoyalCount > 0) {
            declineRatio = (int) Math.round((double) atRiskLoyalCount / totalLoyalCount * 100);
        }

        log.info("매장({}) 이탈 위험 단골 조회 - 이탈 위험: {}명, 전체 단골: {}명, 비율: {}%",
                store.getName(), atRiskLoyalCount, totalLoyalCount, declineRatio);

        return new DeclinedLoyalSummaryResponseDto(
                (int) atRiskLoyalCount,
                declineRatio
        );
    }

    // 고객 목록 조회
    @Override
    @Transactional(readOnly = true)
    public CustomerListResponseDto findCustomerList(Store store, String segment, Pageable pageable) {
        Page<Customer> customerPage;
        LocalDate now = LocalDate.now();

        switch (segment.toLowerCase()) {
            case "all":
                // 전체 고객 조회(최근 방문일 순으로 정렬)
                customerPage = customerRepository.findByStoreOrderByLastVisitDateDesc(store, pageable);
                break;

            case "loyal":
                // LOYAL + AT_RISK_LOYAL 고객 조회(충성도 점수 순)
                List<CustomerSegment> loyalSegments = List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL);
                customerPage = customerRepository.findByStoreAndSegmentInOrderByLoyaltyScoreDesc(
                        store, loyalSegments, pageable);
                break;

            case "at_risk_loyal":
                // AT_RISK_LOYAL 고객 조회(충성도 점수 순)
                customerPage = customerRepository.findByStoreAndSegmentOrderByLoyaltyScoreDesc(
                        store, CustomerSegment.AT_RISK_LOYAL, pageable);
                break;

            case "churn_risk":
                // CHURN_RISK 고객 조회(충성도 점수 순)
                customerPage = customerRepository.findByStoreAndSegmentOrderByLoyaltyScoreDesc(
                        store, CustomerSegment.CHURN_RISK, pageable);
                break;

            default:
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<CustomerSummaryDto> customerSummaries = customerPage.getContent().stream()
                .map(customer -> {
                    int daysSinceLastVisit = (int) ChronoUnit.DAYS.between(customer.getLastVisitDate(), now);

                    return new CustomerSummaryDto(
                            customer.getId(),
                            customer.getName(),
                            daysSinceLastVisit,
                            customer.getTotalVisitCount(),
                            customer.getLoyaltyScore()
                    );
                })
                .collect(Collectors.toList());

        log.info("매장({}) 고객 목록 조회 - segment: {}, page: {}, size: {}, 조회된 고객 수: {}",
                store.getName(), segment, pageable.getPageNumber(), pageable.getPageSize(), customerSummaries.size());

        return new CustomerListResponseDto(
                customerSummaries,
                pageable.getPageSize(),
                pageable.getPageNumber(),
                customerPage.hasNext()
        );
    }

    // 고객 상세 정보 조회
    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponseDto findCustomerDetail(Store store, Long customerId) {
        Customer customer = customerRepository.findByStoreAndId(store, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        LocalDate now = LocalDate.now();
        int daysSinceLastVisit = (int) ChronoUnit.DAYS.between(customer.getLastVisitDate(), now);

        // 이탈 위험 수준 계산
        String churnRiskLevel = determineChurnRiskLevel(customer.getSegment());

        // 최근 6개월 방문 데이터 조회 (현재 달 포함)
        DateRange dateRange = DateRangeCalculator.getLastSixMonthsRange(now);
        List<DailyVisit> visits = dailyVisitRepository.findByCustomerIdAndDateRange(
                customerId, dateRange.startDate(), dateRange.endDate());

        // 월별 방문 횟수 집계
        List<AnalyticsReponseDto> analytics = calculateMonthlyVisits(visits, now);

        log.info("매장({}) 고객({}) 상세 조회 - 총 방문: {}회, 충성도: {}",
                store.getName(), customer.getName(), customer.getTotalVisitCount(), customer.getLoyaltyScore());

        return new CustomerDetailResponseDto(
                customer.getId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getTotalAmount().longValue(),
                customer.getLoyaltyScore(),
                churnRiskLevel,
                customer.getSegment().toString(),
                customer.getPoints(),
                customer.getTotalVisitCount(),
                daysSinceLastVisit,
                analytics
        );
    }

    // 이탈 위험 수준 판단
    private String determineChurnRiskLevel(CustomerSegment segment) {
        return switch (segment) {
            case CHURN_RISK -> "HIGH";
            case AT_RISK_LOYAL -> "MEDIUM";
            default -> "LOW";
        };
    }

    // 6개월 간 방문 횟수 계산
    private List<AnalyticsReponseDto> calculateMonthlyVisits(List<DailyVisit> visits, LocalDate referenceDate) {
        YearMonth currentMonth = YearMonth.from(referenceDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<YearMonth, Integer> visitCountByMonth = new HashMap<>();
        for (DailyVisit visit : visits) {
            YearMonth month = YearMonth.from(visit.getVisitDate());
            visitCountByMonth.put(month, visitCountByMonth.getOrDefault(month, 0) + 1);
        }

        // 이전 5개월 + 현재 달
        List<AnalyticsReponseDto> analytics = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            int count = visitCountByMonth.getOrDefault(targetMonth, 0);

            analytics.add(new AnalyticsReponseDto(
                    targetMonth.format(formatter),
                    count
            ));
        }

        return analytics;
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
                .orElse(180); // 방문 기록이 없으면 매우 큰 값

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

    // ai 분석 결과 바탕으로 고객 정보 업데이트
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