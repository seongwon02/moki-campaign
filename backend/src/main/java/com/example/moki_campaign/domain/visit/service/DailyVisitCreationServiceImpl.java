package com.example.moki_campaign.domain.visit.service;

import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.repository.CustomerRepository;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import com.example.moki_campaign.domain.visit.repository.DailyVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyVisitCreationServiceImpl implements DailyVisitCreationService{

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository; // CustomerRepository 주입
    private final DailyVisitRepository dailyVisitRepository;

    private static final int FIXED_AMOUNT = 3900;
    private static final int MIN_VISITORS = 50;
    private static final int MAX_VISITORS = 100;

    // 매일 일간 방문 고객 기록 조회
    // 키오스크로 받아오지 않고 임의의 값을 넣고 있음
    // 추후 해당 로직 변경 필요
    @Override
    @Transactional
    public void createRandomDailyVisitsForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 모든 매장 조회
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            return;
        }

        List<Customer> allCustomers = customerRepository.findAll();

        Map<Store, List<Customer>> customersByStore = allCustomers.stream()
                .filter(c -> c.getStore() != null)
                .collect(Collectors.groupingBy(Customer::getStore));

        List<DailyVisit> visitsToSave = new ArrayList<>();
        List<Long> allCustomerIdsToUpdate = new ArrayList<>();

        Random random = new Random();

        // 랜덤 방문 데이터 생성
        for (Store store : stores) {
            List<Customer> storeCustomers = customersByStore.getOrDefault(store, List.of());

            if (storeCustomers.isEmpty()) {
                continue;
            }

            int randomCount = random.nextInt(MAX_VISITORS - MIN_VISITORS + 1) + MIN_VISITORS;
            int actualCount = Math.min(storeCustomers.size(), randomCount);

            List<Customer> shuffledCustomers = new ArrayList<>(storeCustomers);
            Collections.shuffle(shuffledCustomers);

            List<Customer> selectedCustomers = shuffledCustomers.subList(0, actualCount);

            for (Customer customer : selectedCustomers) {
                DailyVisit newVisit = DailyVisit.builder()
                        .store(store)
                        .customer(customer)
                        .visitDate(yesterday)
                        .amount(FIXED_AMOUNT)
                        .build();
                visitsToSave.add(newVisit);
            }

            allCustomerIdsToUpdate.addAll(
                    selectedCustomers.stream()
                            .map(Customer::getId)
                            .collect(Collectors.toList())
            );

        }

        // 방문한 고객들에 대한 정보 일괄 업데이트
        if (!allCustomerIdsToUpdate.isEmpty()) {

            customerRepository.batchUpdateCustomerVisitStats(
                    allCustomerIdsToUpdate,
                    yesterday,
                    FIXED_AMOUNT
            );
        } else {
            log.info("업데이트할 고객이 없습니다.");
        }

        // 생성된 모든 Visit 데이터를 DB에 일괄 저장
        if (!visitsToSave.isEmpty()) {
            dailyVisitRepository.saveAll(visitsToSave);
        } else {
            log.info("저장할 방문 데이터가 없습니다.");
        }
    }
}