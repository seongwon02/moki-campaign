package com.example.moki_campaign.domain.customer.repository;

import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 특정 매장에 속한 모든 고객 조회
    List<Customer> findAllByStore(Store store);

    Optional<Customer> findByStoreAndId(Store store, Long customerId);

    Page<Customer> findByStoreOrderByLastVisitDateDesc(Store store, Pageable pageable);

    Page<Customer> findByStoreAndSegmentOrderByLoyaltyScoreDesc(Store store, CustomerSegment segment, Pageable pageable);

    Page<Customer> findByStoreAndSegmentInOrderByLoyaltyScoreDesc(Store store, List<CustomerSegment> segments, Pageable pageable);

    long countByStoreAndSegment(Store store, CustomerSegment segment);

    long countByStoreAndSegmentIn(Store store, List<CustomerSegment> segments);

    // AI 분석 결과 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET " +
            "c.segment = :segment, " +
            "c.loyaltyScore = :loyaltyScore " +
            "WHERE c.id = :id")
    void updateSegmentAndLoyaltyScore(@Param("id") Long id,
                                      @Param("segment") CustomerSegment segment,
                                      @Param("loyaltyScore") int loyaltyScore);

    // 방문 고객 정보 최신화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET " +
            "c.totalVisitCount = c.totalVisitCount + 1, " +
            "c.totalAmount = c.totalAmount + :amount, " +
            "c.lastVisitDate = :visitDate " +
            "WHERE c.id IN :customerIds")
    void batchUpdateCustomerVisitStats(@Param("customerIds") List<Long> customerIds,
                                       @Param("visitDate") LocalDate visitDate,
                                       @Param("amount") Integer amount);
}
