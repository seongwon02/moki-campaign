package com.example.moki_campaign.customer;

import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.VisitGraphItemDto;
import com.example.moki_campaign.domain.customer.dto.response.VisitGraphResponseDto;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.customer.service.CustomerServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DailyVisitRepository dailyVisitRepository;
    @Mock
    private AiClient aiClient;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() throws Exception {
        CustomerServiceImpl realService = new CustomerServiceImpl(
                storeRepository,
                customerRepository,
                dailyVisitRepository,
                aiClient,
                null
        );

        customerService = spy(realService);

        Field selfField = CustomerServiceImpl.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(customerService, customerService);
    }

    @Test
    void 여러매장_분석_성공() {
        // Given
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        when(storeRepository.findAll()).thenReturn(List.of(store1, store2));

        doNothing().when(customerService).analyzeStore(any(Store.class));

        // When
        customerService.analyzeAllStores();

        // Then
        verify(storeRepository, times(1)).findAll();
        verify(customerService, times(1)).analyzeStore(store1); // store1 호출 검증
        verify(customerService, times(1)).analyzeStore(store2); // store2 호출 검증
    }

    @Test
    void 매장_1개_실패해도_정상적으로_나머지_매장_분석() {
        // Given
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        when(storeRepository.findAll()).thenReturn(List.of(store1, store2));

        doNothing().when(customerService).analyzeStore(store1);
        doThrow(new RuntimeException("DB Connection Error"))
                .when(customerService).analyzeStore(store2);

        // When
        customerService.analyzeAllStores();

        // Then
        verify(customerService, times(1)).analyzeStore(store1);
        verify(customerService, times(1)).analyzeStore(store2);
    }

    @Test
    void 분석할_매장_없으면_종료() {
        // Given
        when(storeRepository.findAll()).thenReturn(List.of());

        // When
        customerService.analyzeAllStores();

        // Then
        verify(customerService, never()).analyzeStore(any(Store.class));
    }

    @Test
    void 고객분석_정상적으로_완료() {
        // Given
        Store store = mock(Store.class);
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(1L);
        when(customer.getTotalAmount()).thenReturn(50000);
        when(customer.getTotalVisitCount()).thenReturn(1);
        when(customer.getLastVisitDate()).thenReturn(LocalDate.now().minusWeeks(1));

        DailyVisit visit = mock(DailyVisit.class);
        when(visit.getCustomer()).thenReturn(customer);
        when(visit.getVisitDate()).thenReturn(LocalDate.now().minusWeeks(1));

        AiCustomerDataOutputDto aiOutput = new AiCustomerDataOutputDto("1", "LOYAL", 0.958);
        AiCustomerDataResponseDto aiResponse = new AiCustomerDataResponseDto(List.of(aiOutput));

        when(customerRepository.findAllByStore(store)).thenReturn(List.of(customer));
        when(dailyVisitRepository.findByStoreAndDateRangeWithCustomer(eq(store), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(visit));
        when(aiClient.analyzeCustomers(anyList())).thenReturn(aiResponse);

        // When
        customerService.analyzeStore(store);

        // Then
        ArgumentCaptor<List<AiCustomerDataInputDto>> inputCaptor = ArgumentCaptor.forClass(List.class);
        verify(aiClient).analyzeCustomers(inputCaptor.capture());

        List<AiCustomerDataInputDto> capturedInput = inputCaptor.getValue();
        assertEquals(1, capturedInput.size());
        AiCustomerDataInputDto inputDto = capturedInput.get(0);
        assertEquals("1", inputDto.customerId());
        assertEquals(50000.0, inputDto.amount());
        assertEquals(1, inputDto.totalVisits());

        // 주별 데이터 검증 - 최소한 하나의 주에 방문 기록이 있어야 함
        int totalVisitsInWeeks = inputDto.visits1WeekAgo() + inputDto.visits2WeekAgo() +
                                  inputDto.visits3WeekAgo() + inputDto.visits4WeekAgo() +
                                  inputDto.visits5WeekAgo() + inputDto.visits6WeekAgo() +
                                  inputDto.visits7WeekAgo() + inputDto.visits8WeekAgo();
        assertThat(totalVisitsInWeeks).isGreaterThanOrEqualTo(1);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CustomerSegment> segmentCaptor = ArgumentCaptor.forClass(CustomerSegment.class);
        ArgumentCaptor<Integer> scoreCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(customerRepository, times(1)).updateSegmentAndLoyaltyScore(
                idCaptor.capture(),
                segmentCaptor.capture(),
                scoreCaptor.capture()
        );

        assertEquals(1L, idCaptor.getValue());
        assertEquals(CustomerSegment.LOYAL, segmentCaptor.getValue());
        assertEquals(96, scoreCaptor.getValue());
    }

    @Test
    void 분석할_고객이_없는_경우() {
        // Given
        Store store = mock(Store.class);
        when(customerRepository.findAllByStore(store)).thenReturn(List.of());

        // When
        customerService.analyzeStore(store);

        // Then
        verify(aiClient, never()).analyzeCustomers(anyList());
        verify(dailyVisitRepository, never()).findByStoreAndDateRangeWithCustomer(any(), any(), any());
        verify(customerRepository, never()).updateSegmentAndLoyaltyScore(anyLong(), any(), anyInt());
    }

    @Test
    void AI분석결과가_비었으면_DB_업데이트_안함() {
        // Given
        Store store = mock(Store.class);
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(1L);
        when(customer.getTotalAmount()).thenReturn(50000);
        when(customer.getTotalVisitCount()).thenReturn(1);
        when(customer.getLastVisitDate()).thenReturn(LocalDate.now().minusWeeks(1));

        when(customerRepository.findAllByStore(store)).thenReturn(List.of(customer));
        when(dailyVisitRepository.findByStoreAndDateRangeWithCustomer(any(), any(), any())).thenReturn(List.of());

        AiCustomerDataResponseDto aiResponse = new AiCustomerDataResponseDto(List.of());
        when(aiClient.analyzeCustomers(anyList())).thenReturn(aiResponse);

        // When
        customerService.analyzeStore(store);

        // Then
        verify(aiClient, times(1)).analyzeCustomers(anyList());
        verify(customerRepository, never()).updateSegmentAndLoyaltyScore(anyLong(), any(), anyInt());
    }

    @Test
    void AI서버_호출_실패() {
        // Given
        Store store = mock(Store.class);
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(1L);
        when(customer.getTotalAmount()).thenReturn(50000);
        when(customer.getTotalVisitCount()).thenReturn(1);
        when(customer.getLastVisitDate()).thenReturn(LocalDate.now().minusWeeks(1));

        when(customerRepository.findAllByStore(store)).thenReturn(List.of(customer));
        when(dailyVisitRepository.findByStoreAndDateRangeWithCustomer(any(), any(), any())).thenReturn(List.of());

        when(aiClient.analyzeCustomers(anyList())).thenThrow(new RuntimeException("AI Server 500 Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            customerService.analyzeStore(store);
        });

        verify(customerRepository, never()).updateSegmentAndLoyaltyScore(anyLong(), any(), anyInt());
    }

    @Nested
    @DisplayName("이탈 위험 단골 정보 조회")
    class FindDeclinedLoyalInfoTest {

        @Test
        @DisplayName("AT_RISK_LOYAL 고객 수와 비율을 정상적으로 계산")
        void 이탈위험_단골_정보_조회_성공() {
            // Given
            Store store = createStore(1L, "테스트 매장");

            // AT_RISK_LOYAL 고객: 7명
            when(customerRepository.countByStoreAndSegment(store, CustomerSegment.AT_RISK_LOYAL))
                    .thenReturn(7L);

            // LOYAL + AT_RISK_LOYAL 전체: 20명
            when(customerRepository.countByStoreAndSegmentIn(store, List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL)))
                    .thenReturn(20L);

            // When
            DeclinedLoyalSummaryResponseDto result = customerService.findDeclinedLoyalInfo(store);

            // Then
            assertThat(result.declineCount()).isEqualTo(7);
            assertThat(result.declineRatio()).isEqualTo(35); // 7/20 * 100 = 35%

            verify(customerRepository).countByStoreAndSegment(store, CustomerSegment.AT_RISK_LOYAL);
            verify(customerRepository).countByStoreAndSegmentIn(store, List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL));
        }
    }

    @Nested
    @DisplayName("고객 목록 조회")
    class FindCustomerListTest {

        @Test
        @DisplayName("segment=all - 전체 고객을 최근 방문일 순으로 조회")
        void 전체_고객_조회_최근_방문일_순() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Pageable pageable = PageRequest.of(0, 20, Sort.by(
                    Sort.Order.desc("lastVisitDate"),
                    Sort.Order.desc("loyaltyScore"),
                    Sort.Order.desc("id")
            ));

            LocalDate now = LocalDate.now();
            List<Customer> customers = List.of(
                    createMockCustomer(1L, store, "고객1", now.minusDays(1), 10, 85, CustomerSegment.LOYAL),
                    createMockCustomer(2L, store, "고객2", now.minusDays(5), 15, 75, CustomerSegment.GENERAL),
                    createMockCustomer(3L, store, "고객3", now.minusDays(10), 20, 65, CustomerSegment.CHURN_RISK)
            );

            Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
            when(customerRepository.findByStore(store, pageable))
                    .thenReturn(customerPage);

            // When
            CustomerListResponseDto result = customerService.findCustomerList(store, "all", pageable);

            // Then
            assertThat(result.customers()).hasSize(3);
            assertThat(result.customers().get(0).visitDayAgo()).isEqualTo(1);
            assertThat(result.customers().get(1).visitDayAgo()).isEqualTo(5);
            assertThat(result.customers().get(2).visitDayAgo()).isEqualTo(10);
            assertThat(result.hasNext()).isFalse();

            verify(customerRepository).findByStore(store, pageable);
        }

        @Test
        @DisplayName("segment=loyal - LOYAL과 AT_RISK_LOYAL 고객을 충성도 점수 순으로 조회")
        void 단골_고객_조회_충성도_점수_순() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Pageable pageable = PageRequest.of(0, 20, Sort.by(
                    Sort.Order.desc("loyaltyScore"),
                    Sort.Order.desc("lastVisitDate"),
                    Sort.Order.desc("id")
            ));

            LocalDate now = LocalDate.now();
            List<Customer> customers = List.of(
                    createMockCustomer(1L, store, "충성고객1", now.minusDays(3), 20, 95, CustomerSegment.LOYAL),
                    createMockCustomer(2L, store, "이탈위험고객1", now.minusDays(7), 15, 85, CustomerSegment.AT_RISK_LOYAL),
                    createMockCustomer(3L, store, "충성고객2", now.minusDays(2), 25, 80, CustomerSegment.LOYAL)
            );

            Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
            List<CustomerSegment> loyalSegments = List.of(CustomerSegment.LOYAL, CustomerSegment.AT_RISK_LOYAL);

            when(customerRepository.findByStoreAndSegmentIn(store, loyalSegments, pageable))
                    .thenReturn(customerPage);

            // When
            CustomerListResponseDto result = customerService.findCustomerList(store, "loyal", pageable);

            // Then
            assertThat(result.customers()).hasSize(3);
            assertThat(result.customers().get(0).loyaltyScore()).isEqualTo(95);
            assertThat(result.customers().get(1).loyaltyScore()).isEqualTo(85);
            assertThat(result.customers().get(2).loyaltyScore()).isEqualTo(80);

            verify(customerRepository).findByStoreAndSegmentIn(store, loyalSegments, pageable);
        }

        @Test
        @DisplayName("segment=risk_at_loyal - AT_RISK_LOYAL 고객만 충성도 점수 순으로 조회")
        void 이탈위험_단골_고객_조회() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Pageable pageable = PageRequest.of(0, 20, Sort.by(
                    Sort.Order.desc("loyaltyScore"),
                    Sort.Order.desc("lastVisitDate"),
                    Sort.Order.desc("id")
            ));

            LocalDate now = LocalDate.now();
            List<Customer> customers = List.of(
                    createMockCustomer(1L, store, "이탈위험1", now.minusDays(10), 18, 75, CustomerSegment.AT_RISK_LOYAL),
                    createMockCustomer(2L, store, "이탈위험2", now.minusDays(15), 22, 70, CustomerSegment.AT_RISK_LOYAL)
            );

            Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
            when(customerRepository.findByStoreAndSegment(store, CustomerSegment.AT_RISK_LOYAL, pageable))
                    .thenReturn(customerPage);

            // When
            CustomerListResponseDto result = customerService.findCustomerList(store, "at_risk_loyal", pageable);

            // Then
            assertThat(result.customers()).hasSize(2);
            assertThat(result.customers().get(0).loyaltyScore()).isEqualTo(75);
            assertThat(result.customers().get(1).loyaltyScore()).isEqualTo(70);

            verify(customerRepository).findByStoreAndSegment(store, CustomerSegment.AT_RISK_LOYAL, pageable);
        }

        @Test
        @DisplayName("segment=churn_risk - CHURN_RISK 고객을 충성도 점수 순으로 조회")
        void 이탈_고객_조회() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Pageable pageable = PageRequest.of(0, 20, Sort.by(
                    Sort.Order.desc("loyaltyScore"),
                    Sort.Order.desc("lastVisitDate"),
                    Sort.Order.desc("id")
            ));

            LocalDate now = LocalDate.now();
            List<Customer> customers = List.of(
                    createMockCustomer(1L, store, "이탈고객1", now.minusDays(30), 5, 40, CustomerSegment.CHURN_RISK)
            );

            List<CustomerSegment> riskSegments = List.of(CustomerSegment.CHURN_RISK, CustomerSegment.AT_RISK_LOYAL);
            Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
            when(customerRepository.findByStoreAndSegmentIn(store, riskSegments, pageable))
                    .thenReturn(customerPage);

            // When
            CustomerListResponseDto result = customerService.findCustomerList(store, "churn_risk", pageable);

            // Then
            assertThat(result.customers()).hasSize(1);
            assertThat(result.customers().get(0).customerId()).isEqualTo(1L);

            verify(customerRepository).findByStoreAndSegmentIn(store, riskSegments, pageable);
        }

        @Test
        @DisplayName("잘못된 segment 값으로 조회 시 예외 발생")
        void 잘못된_segment_값_예외() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Pageable pageable = PageRequest.of(0, 20);

            // When & Then
            assertThatThrownBy(() -> customerService.findCustomerList(store, "invalid_segment", pageable))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("고객 상세 정보 조회")
    class FindCustomerDetailTest {

        @Test
        @DisplayName("고객 상세 정보를 정상적으로 조회")
        void 고객_상세_정보_조회_성공() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;
            LocalDate now = LocalDate.now();

            Customer customer = createMockCustomerDetail(
                    customerId,
                    store,
                    "홍길동",
                    "010-1234-5678",
                    500000,
                    1500,
                    now.minusDays(5),
                    23,
                    85,
                    CustomerSegment.LOYAL
            );

            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));


            // When
            CustomerDetailResponseDto result = customerService.findCustomerDetail(store, customerId);

            // Then
            assertThat(result.customerId()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("홍길동");
            assertThat(result.phoneNumber()).isEqualTo("010-1234-5678");
            assertThat(result.totalSpent()).isEqualTo(500000L);
            assertThat(result.loyaltyScore()).isEqualTo(85);
            assertThat(result.churnRiskLevel()).isEqualTo("LOW");
            assertThat(result.segment()).isEqualTo("LOYAL");
            assertThat(result.currentPoints()).isEqualTo(1500);
            assertThat(result.totalVisitCount()).isEqualTo(23);
            assertThat(result.visitDayAgo()).isEqualTo(5);
        }

        @Test
        @DisplayName("이탈 위험 수준이 올바르게 계산 - CHURN_RISK는 HIGH")
        void 이탈위험_수준_HIGH() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            Customer customer = createMockCustomer(customerId, store, "이탈고객", LocalDate.now().minusDays(30), 5, 30, CustomerSegment.CHURN_RISK);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // When
            CustomerDetailResponseDto result = customerService.findCustomerDetail(store, customerId);

            // Then
            assertThat(result.churnRiskLevel()).isEqualTo("HIGH");
            assertThat(result.segment()).isEqualTo("CHURN_RISK");
            assertThat(result.visitDayAgo()).isEqualTo(30);
        }

        @Test
        @DisplayName("이탈 위험 수준이 올바르게 계산 - AT_RISK_LOYAL는 MEDIUM")
        void 이탈위험_수준_MEDIUM() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            Customer customer = createMockCustomer(customerId, store, "이탈위험단골", LocalDate.now().minusDays(10), 15, 70, CustomerSegment.AT_RISK_LOYAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // When
            CustomerDetailResponseDto result = customerService.findCustomerDetail(store, customerId);

            // Then
            assertThat(result.churnRiskLevel()).isEqualTo("MEDIUM");
            assertThat(result.segment()).isEqualTo("AT_RISK_LOYAL");
            assertThat(result.visitDayAgo()).isEqualTo(10);
        }

        @Test
        @DisplayName("이탈 위험 수준이 올바르게 계산 - LOYAL과 GENERAL은 LOW")
        void 이탈위험_수준_LOW() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            Customer customer = createMockCustomer(customerId, store, "충성고객", LocalDate.now().minusDays(2), 30, 95, CustomerSegment.LOYAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // When
            CustomerDetailResponseDto result = customerService.findCustomerDetail(store, customerId);

            // Then
            assertThat(result.churnRiskLevel()).isEqualTo("LOW");
            assertThat(result.segment()).isEqualTo("LOYAL");
            assertThat(result.visitDayAgo()).isEqualTo(2);
        }

        @Test
        @DisplayName("존재하지 않는 고객 조회 시 예외 발생")
        void 존재하지_않는_고객_예외() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 999L;

            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerService.findCustomerDetail(store, customerId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("고객 방문 빈도 그래프 조회")
    class FindCustomerVisitGraphTest {

        @Test
        @DisplayName("월별 그래프 조회 - 최근 6개월 데이터 정상 반환")
        void 월별_그래프_조회_성공() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;
            LocalDate now = LocalDate.now();

            Customer customer = createMockCustomer(customerId, store, "고객1", now.minusDays(5), 20, 85, CustomerSegment.LOYAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // 최근 6개월 방문 데이터 생성
            YearMonth currentMonth = YearMonth.from(now);
            List<DailyVisit> visits = new ArrayList<>();
            for (int i = 5; i >= 0; i--) {
                YearMonth targetMonth = currentMonth.minusMonths(i);
                LocalDate visitDate = targetMonth.atDay(10);
                visits.add(createMockDailyVisit((long) (i + 1), customerId, store, visitDate, 10000));
            }

            when(dailyVisitRepository.findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(visits);

            // When
            VisitGraphResponseDto result = customerService.findCustomerVisitGraph(store, customerId, "month");

            // Then
            assertThat(result.graph()).hasSize(6);

            // 라벨이 yyyy-MM 형식인지 확인
            for (VisitGraphItemDto item : result.graph()) {
                assertThat(item.label()).matches("\\d{4}-\\d{2}");
                assertThat(item.count()).isGreaterThanOrEqualTo(0);
            }

            verify(customerRepository).findByStoreAndId(store, customerId);
            verify(dailyVisitRepository).findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @DisplayName("주별 그래프 조회 - 최근 8주 데이터 정상 반환")
        void 주별_그래프_조회_성공() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;
            LocalDate now = LocalDate.now();

            Customer customer = createMockCustomer(customerId, store, "고객1", now.minusDays(5), 15, 80, CustomerSegment.GENERAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // 최근 8주 방문 데이터 생성
            List<DailyVisit> visits = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                LocalDate visitDate = now.minusWeeks(i);
                visits.add(createMockDailyVisit((long) (i + 1), customerId, store, visitDate, 10000));
            }

            when(dailyVisitRepository.findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(visits);

            // When
            VisitGraphResponseDto result = customerService.findCustomerVisitGraph(store, customerId, "week");

            // Then
            assertThat(result.graph()).hasSize(8);

            // 라벨이 yyyy-MM-dd 형식인지 확인 (주의 시작일 = 월요일)
            for (VisitGraphItemDto item : result.graph()) {
                assertThat(item.label()).matches("\\d{4}-\\d{2}-\\d{2}");
                assertThat(item.count()).isGreaterThanOrEqualTo(0);
            }

            verify(customerRepository).findByStoreAndId(store, customerId);
            verify(dailyVisitRepository).findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @DisplayName("월별 그래프 - 방문 데이터가 없으면 모두 0으로 반환")
        void 월별_그래프_방문_데이터_없음() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            Customer customer = createMockCustomer(customerId, store, "신규고객", LocalDate.now().minusDays(1), 0, 50, CustomerSegment.GENERAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            when(dailyVisitRepository.findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            // When
            VisitGraphResponseDto result = customerService.findCustomerVisitGraph(store, customerId, "month");

            // Then
            assertThat(result.graph()).hasSize(6);
            for (VisitGraphItemDto item : result.graph()) {
                assertThat(item.count()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("주별 그래프 - 방문 데이터가 없으면 모두 0으로 반환")
        void 주별_그래프_방문_데이터_없음() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            Customer customer = createMockCustomer(customerId, store, "신규고객", LocalDate.now().minusDays(1), 0, 50, CustomerSegment.GENERAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            when(dailyVisitRepository.findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            // When
            VisitGraphResponseDto result = customerService.findCustomerVisitGraph(store, customerId, "week");

            // Then
            assertThat(result.graph()).hasSize(8);
            for (VisitGraphItemDto item : result.graph()) {
                assertThat(item.count()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("잘못된 period 값으로 조회 시 예외 발생")
        void 잘못된_period_값_예외() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;

            // When & Then
            // period 검증이 먼저 일어나므로 customerRepository mock 불필요
            assertThatThrownBy(() -> customerService.findCustomerVisitGraph(store, customerId, "invalid"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("존재하지 않는 고객 조회 시 예외 발생")
        void 존재하지_않는_고객_예외() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 999L;

            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerService.findCustomerVisitGraph(store, customerId, "month"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_NOT_FOUND);
        }

        @Test
        @DisplayName("월별 그래프 - 여러 달에 방문 데이터가 있을 때 정확한 집계")
        void 월별_그래프_여러_달_집계() {
            // Given
            Store store = createStore(1L, "테스트 매장");
            Long customerId = 1L;
            LocalDate now = LocalDate.now();

            Customer customer = createMockCustomer(customerId, store, "단골고객", now.minusDays(2), 25, 90, CustomerSegment.LOYAL);
            when(customerRepository.findByStoreAndId(store, customerId))
                    .thenReturn(Optional.of(customer));

            // 현재 달에 3번, 1달 전에 2번, 2달 전에 1번
            YearMonth currentMonth = YearMonth.from(now);
            List<DailyVisit> visits = new ArrayList<>();

            // 현재 달 3번
            for (int i = 0; i < 3; i++) {
                visits.add(createMockDailyVisit((long) (visits.size() + 1), customerId, store,
                    currentMonth.atDay(5 + i * 5), 10000));
            }

            // 1달 전 2번
            for (int i = 0; i < 2; i++) {
                visits.add(createMockDailyVisit((long) (visits.size() + 1), customerId, store,
                    currentMonth.minusMonths(1).atDay(10 + i * 5), 10000));
            }

            // 2달 전 1번
            visits.add(createMockDailyVisit((long) (visits.size() + 1), customerId, store,
                currentMonth.minusMonths(2).atDay(15), 10000));

            when(dailyVisitRepository.findByCustomerIdAndDateRange(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(visits);

            // When
            VisitGraphResponseDto result = customerService.findCustomerVisitGraph(store, customerId, "month");

            // Then
            assertThat(result.graph()).hasSize(6);

            // 마지막 달(현재 달)이 3번 방문인지 확인
            VisitGraphItemDto lastMonth = result.graph().get(5);
            assertThat(lastMonth.label()).isEqualTo(currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            assertThat(lastMonth.count()).isEqualTo(3);
        }
    }

    // Helper methods for creating test data
    private Store createStore(Long id, String name) {
        return Store.builder()
                .businessNumber("123-45-67890")
                .password("password")
                .name(name)
                .phoneNumber("02-1234-5678")
                .build();
    }

    private List<DailyVisit> createMonthlyVisits(Long customerId, Store store, LocalDate referenceDate) {
        List<DailyVisit> visits = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(referenceDate);

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate visitDate = targetMonth.atDay(15);
            visits.add(createMockDailyVisit((long) (i + 1), customerId, store, visitDate, 10000));
        }

        return visits;
    }

    private Customer createMockCustomer(Long id, Store store, String name, LocalDate lastVisitDate, Integer totalVisitCount, Integer loyaltyScore, CustomerSegment segment) {
        Customer customer = mock(Customer.class, withSettings().lenient());
        when(customer.getId()).thenReturn(id);
        when(customer.getStore()).thenReturn(store);
        when(customer.getName()).thenReturn(name);
        when(customer.getPhoneNumber()).thenReturn("010-0000-0000");
        when(customer.getTotalAmount()).thenReturn(100000);
        when(customer.getPoints()).thenReturn(1000);
        when(customer.getSegment()).thenReturn(segment);
        when(customer.getLoyaltyScore()).thenReturn(loyaltyScore);
        when(customer.getTotalVisitCount()).thenReturn(totalVisitCount);
        when(customer.getLastVisitDate()).thenReturn(lastVisitDate);
        return customer;
    }

    private Customer createMockCustomerDetail(Long id, Store store, String name, String phoneNumber, Integer totalAmount, Integer points,
                                              LocalDate lastVisitDate, Integer totalVisitCount, Integer loyaltyScore, CustomerSegment segment) {
        Customer customer = mock(Customer.class, withSettings().lenient());
        when(customer.getId()).thenReturn(id);
        when(customer.getStore()).thenReturn(store);
        when(customer.getName()).thenReturn(name);
        when(customer.getPhoneNumber()).thenReturn(phoneNumber);
        when(customer.getTotalAmount()).thenReturn(totalAmount);
        when(customer.getPoints()).thenReturn(points);
        when(customer.getSegment()).thenReturn(segment);
        when(customer.getLoyaltyScore()).thenReturn(loyaltyScore);
        when(customer.getTotalVisitCount()).thenReturn(totalVisitCount);
        when(customer.getLastVisitDate()).thenReturn(lastVisitDate);
        return customer;
    }

    private DailyVisit createMockDailyVisit(Long id, Long customerId, Store store, LocalDate visitDate, Integer amount) {
        Customer customer = mock(Customer.class, withSettings().lenient());
        when(customer.getId()).thenReturn(customerId);

        DailyVisit visit = mock(DailyVisit.class, withSettings().lenient());
        when(visit.getId()).thenReturn(id);
        when(visit.getStore()).thenReturn(store);
        when(visit.getCustomer()).thenReturn(customer);
        when(visit.getVisitDate()).thenReturn(visitDate);
        when(visit.getAmount()).thenReturn(amount);
        return visit;
    }
}
