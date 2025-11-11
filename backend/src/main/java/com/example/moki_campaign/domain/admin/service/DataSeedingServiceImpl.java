package com.example.moki_campaign.domain.admin.service;

import com.example.moki_campaign.domain.admin.dto.CustomerSeedRequestDto;
import com.example.moki_campaign.domain.admin.dto.DailyVisitSeedRequestDto;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeedingServiceImpl implements DataSeedingService {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final DailyVisitRepository dailyVisitRepository;

    @Override
    @Transactional
    public Customer createSpecificCustomer(CustomerSeedRequestDto dto) {
        log.warn("========= 수동 고객 시딩 1건 실행 (Admin) =========");

        // 1. Store 엔티티 조회
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND,
                        String.format("ID(%d)에 해당하는 매장을 찾을 수 없습니다.", dto.getStoreId())));

        // 2. DTO -> Entity 변환 (Builder 사용)
        Customer customer = Customer.builder()
                .store(store)
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .totalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : 0)
                .points(dto.getPoints() != null ? dto.getPoints() : 0)
                .segment(dto.getSegment() != null ? dto.getSegment() : CustomerSegment.GENERAL)
                .loyaltyScore(dto.getLoyaltyScore() != null ? dto.getLoyaltyScore() : 0)
                .totalVisitCount(dto.getTotalVisitCount() != null ? dto.getTotalVisitCount() : 0)
                .lastVisitDate(dto.getLastVisitDate() != null ? dto.getLastVisitDate() : LocalDate.of(2000, 1, 1))
                .build();

        // 3. DB에 저장
        Customer savedCustomer = customerRepository.save(customer);
        log.info("고객(ID: {}) 생성 완료: {}", savedCustomer.getId(), savedCustomer.getName());
        return savedCustomer;
    }

    @Override
    @Transactional
    public DailyVisit createSpecificVisit(DailyVisitSeedRequestDto dto) {
        log.warn("========= 수동 방문기록 시딩 1건 실행 (Admin) =========");

        // 1. Customer 엔티티 조회
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        String.format("ID(%d)에 해당하는 고객을 찾을 수 없습니다.", dto.getCustomerId())));

        // 2. DailyVisit 엔티티 생성
        DailyVisit newVisit = DailyVisit.builder()
                .store(customer.getStore()) // 고객에게 연결된 매장 사용
                .customer(customer)
                .visitDate(dto.getVisitDate())
                .amount(dto.getAmount())
                .build();

        // 3. 방문 기록 저장
        DailyVisit savedVisit = dailyVisitRepository.save(newVisit);
        log.info("방문기록(ID: {}) 생성 완료 (고객 ID: {})", savedVisit.getId(), customer.getId());

        return savedVisit;
    }
}
