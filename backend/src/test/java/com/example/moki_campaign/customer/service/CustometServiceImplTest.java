package com.example.moki_campaign.customer.service;

import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.customer.service.CustomerServiceImpl;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import com.example.moki_campaign.infra.ai.client.AiClient;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataInputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataOutputDto;
import com.example.moki_campaign.infra.ai.dto.AiCustomerDataResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private DailyVisitRepository dailyVisitRepository;
    @Mock
    private AiClient aiClient;

    @Spy
    @InjectMocks
    private CustomerServiceImpl customerService;

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

        DailyVisit visit = mock(DailyVisit.class);
        when(visit.getCustomer()).thenReturn(customer);
        when(visit.getAmount()).thenReturn(50000);
        when(visit.getVisitDate()).thenReturn(LocalDate.now().minusMonths(1));

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

        YearMonth endMonth = YearMonth.now().minusMonths(1);
        if (YearMonth.from(visit.getVisitDate()).equals(endMonth)) {
            assertEquals(1, inputDto.visits1MonthAgo());
        } else if (YearMonth.from(visit.getVisitDate()).equals(endMonth.minusMonths(1))) {
            assertEquals(1, inputDto.visits2MonthAgo());
        }


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
        when(customerRepository.findAllByStore(store)).thenReturn(List.of(customer));
        when(dailyVisitRepository.findByStoreAndDateRangeWithCustomer(any(), any(), any())).thenReturn(List.of());

        when(aiClient.analyzeCustomers(anyList())).thenThrow(new RuntimeException("AI Server 500 Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            customerService.analyzeStore(store);
        });

        verify(customerRepository, never()).updateSegmentAndLoyaltyScore(anyLong(), any(), anyInt());
    }
}