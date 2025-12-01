package com.example.moki_campaign.domain.customer.service;

import com.example.moki_campaign.domain.customer.dto.response.AnalyticsReponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerSummaryDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.VisitGraphItemDto;
import com.example.moki_campaign.domain.customer.dto.response.VisitGraphResponseDto;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.infra.ai.client.AiClient;
import com.example.moki_campaign.infra.ai.dto.request.AiCustomerDataInputDto;
import com.example.moki_campaign.infra.ai.dto.response.AiCustomerDataOutputDto;
import com.example.moki_campaign.infra.ai.dto.response.AiCustomerDataResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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
                pageable = sortForAll(pageable.getPageNumber(), pageable.getPageSize());
                customerPage = customerRepository.findByStore(store, pageable);
                break;

            case "loyal":
                // LOYAL + AT_RISK_LOYAL 고객 조회
                pageable = sortForOthers(pageable.getPageNumber(), pageable.getPageSize());
                List<CustomerSegment> loyalSegments = List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL);
                customerPage = customerRepository.findByStoreAndSegmentIn(
                        store, loyalSegments, pageable);
                break;

            case "at_risk_loyal":
                // AT_RISK_LOYAL 고객 조회
                pageable = sortForOthers(pageable.getPageNumber(), pageable.getPageSize());
                customerPage = customerRepository.findByStoreAndSegment(
                        store, CustomerSegment.AT_RISK_LOYAL, pageable);
                break;

            case "churn_risk":
                // CHURN_RISK + AT_RISK_LOYAL 고객 조회
                pageable = sortForOthers(pageable.getPageNumber(), pageable.getPageSize());
                List<CustomerSegment> riskSegments = List.of(CustomerSegment.CHURN_RISK, CustomerSegment.AT_RISK_LOYAL);
                customerPage = customerRepository.findByStoreAndSegmentIn(
                        store, riskSegments, pageable);
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
                daysSinceLastVisit
        );
    }

    // 고객 방문 빈도 그래프 조회
    @Override
    @Transactional(readOnly = true)
    public VisitGraphResponseDto findCustomerVisitGraph(Store store, Long customerId, String period) {
        // period 검증
        if (!period.equalsIgnoreCase("week") && !period.equalsIgnoreCase("month")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 고객 존재 확인
        Customer customer = customerRepository.findByStoreAndId(store, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        LocalDate now = LocalDate.now();
        List<VisitGraphItemDto> graphData;

        if (period.equalsIgnoreCase("month")) {
            // 월 단위: 최근 6개월
            graphData = calculateMonthlyGraph(customerId, now);
        } else {
            // 주 단위: 최근 8주
            graphData = calculateWeeklyGraph(customerId, now);
        }

        log.info("매장({}) 고객({}) 방문 그래프 조회 - period: {}, 데이터 포인트: {}개",
                store.getName(), customer.getName(), period, graphData.size());

        return new VisitGraphResponseDto(graphData);
    }

    // 월별 방문 그래프 데이터 계산 (최근 6개월)
    private List<VisitGraphItemDto> calculateMonthlyGraph(Long customerId, LocalDate referenceDate) {
        YearMonth currentMonth = YearMonth.from(referenceDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 6개월 전부터 현재 월까지의 날짜 범위 계산
        LocalDate startDate = currentMonth.minusMonths(5).atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        // 방문 데이터 조회
        List<DailyVisit> visits = dailyVisitRepository.findByCustomerIdAndDateRange(
                customerId, startDate, endDate);

        // 월별로 그룹화
        Map<YearMonth, Integer> visitCountByMonth = new HashMap<>();
        for (DailyVisit visit : visits) {
            YearMonth month = YearMonth.from(visit.getVisitDate());
            visitCountByMonth.put(month, visitCountByMonth.getOrDefault(month, 0) + 1);
        }

        // 6개월 데이터 생성 (과거 -> 현재 순서)
        List<VisitGraphItemDto> graphData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            int count = visitCountByMonth.getOrDefault(targetMonth, 0);
            graphData.add(new VisitGraphItemDto(targetMonth.format(formatter), count));
        }

        return graphData;
    }

    // 주별 방문 그래프 데이터 계산 (최근 8주)
    private List<VisitGraphItemDto> calculateWeeklyGraph(Long customerId, LocalDate referenceDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 날짜가 속한 주의 월요일을 구함 (주의 시작일)
        LocalDate currentWeekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 8주 전 월요일부터 현재 주 일요일까지의 날짜 범위
        LocalDate startDate = currentWeekStart.minusWeeks(7);
        LocalDate endDate = currentWeekStart.plusDays(6); // 일요일까지

        // 방문 데이터 조회
        List<DailyVisit> visits = dailyVisitRepository.findByCustomerIdAndDateRange(
                customerId, startDate, endDate);

        // 주별로 그룹화 (각 방문 날짜가 속한 주의 월요일 기준)
        Map<LocalDate, Integer> visitCountByWeek = new HashMap<>();
        for (DailyVisit visit : visits) {
            LocalDate weekStart = visit.getVisitDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            visitCountByWeek.put(weekStart, visitCountByWeek.getOrDefault(weekStart, 0) + 1);
        }

        // 8주 데이터 생성 (과거 -> 현재 순서)
        List<VisitGraphItemDto> graphData = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            LocalDate weekStart = currentWeekStart.minusWeeks(i);
            int count = visitCountByWeek.getOrDefault(weekStart, 0);
            graphData.add(new VisitGraphItemDto(weekStart.format(formatter), count));
        }

        return graphData;
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

    // ai 분석을 위한 요청 데이터 생성 (지난 8주 데이터)
    private List<AiCustomerDataInputDto> prepareDataForAnalysis(Store store) {
        LocalDate now = LocalDate.now();

        // 현재 주의 월요일을 기준으로 8주 전부터 현재 주 일요일까지
        LocalDate currentWeekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startDate = currentWeekStart.minusWeeks(7);  // 8주 전 월요일
        LocalDate endDate = currentWeekStart.plusDays(6);      // 현재 주 일요일

        List<Customer> customers = customerRepository.findAllByStore(store);

        // 매장에 고객이 존재하지 않는 경우
        if (customers.isEmpty()) {
            return List.of();
        }

        List<DailyVisit> visits = dailyVisitRepository
                .findByStoreAndDateRangeWithCustomer(store, startDate, endDate);

        Map<Long, List<DailyVisit>> visitsByCustomer = visits.stream()
                .collect(Collectors.groupingBy(v -> v.getCustomer().getId()));

        List<AiCustomerDataInputDto> result = customers.stream()
                .map(customer -> {
                    List<DailyVisit> customerVisits = visitsByCustomer
                            .getOrDefault(customer.getId(), List.of());
                    return convertToAiInputDto(customer, customerVisits, now);
                })
                .collect(Collectors.toList());

        return result;
    }

    // ai 고객 분석용 dto 생성 (주별 방문 데이터)
    private AiCustomerDataInputDto convertToAiInputDto(
            Customer customer,
            List<DailyVisit> visits,
            LocalDate analysisEndDate) {

        double totalAmount = customer.getTotalAmount();

        int totalVisits = customer.getTotalVisitCount();

        int daysSinceLastVisit = (int) ChronoUnit.DAYS.between(customer.getLastVisitDate(), analysisEndDate);

        // 현재 주의 월요일 기준
        LocalDate currentWeekStart = analysisEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 8주 전부터 1주 전까지 각 주의 방문 횟수 계산
        int visits8WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(7));
        int visits7WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(6));
        int visits6WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(5));
        int visits5WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(4));
        int visits4WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(3));
        int visits3WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(2));
        int visits2WeekAgo = countVisitsInWeek(visits, currentWeekStart.minusWeeks(1));
        int visits1WeekAgo = countVisitsInWeek(visits, currentWeekStart);

        return AiCustomerDataInputDto.builder()
                .customerId(String.valueOf(customer.getId()))
                .amount(totalAmount)
                .totalVisits(totalVisits)
                .daysSinceLastVisit(daysSinceLastVisit)
                .visits8WeekAgo(visits8WeekAgo)
                .visits7WeekAgo(visits7WeekAgo)
                .visits6WeekAgo(visits6WeekAgo)
                .visits5WeekAgo(visits5WeekAgo)
                .visits4WeekAgo(visits4WeekAgo)
                .visits3WeekAgo(visits3WeekAgo)
                .visits2WeekAgo(visits2WeekAgo)
                .visits1WeekAgo(visits1WeekAgo)
                .build();
    }

    // 특정 주 동안의 방문 횟수 계산 (월요일 시작 기준)
    private int countVisitsInWeek(List<DailyVisit> visits, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6); // 일요일까지

        return (int) visits.stream()
                .filter(visit -> {
                    LocalDate visitDate = visit.getVisitDate();
                    return !visitDate.isBefore(weekStart) && !visitDate.isAfter(weekEnd);
                })
                .count();
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

    private Pageable sortForAll(int page, int size) {
        return PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("lastVisitDate"),
                Sort.Order.desc("loyaltyScore"),
                Sort.Order.desc("id")
        ));
    }

    private Pageable sortForOthers(int page, int size) {
        return PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("loyaltyScore"),
                Sort.Order.desc("lastVisitDate"),
                Sort.Order.desc("id")
        ));
    }
}