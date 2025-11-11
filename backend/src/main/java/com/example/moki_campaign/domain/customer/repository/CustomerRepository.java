package com.example.moki_campaign.domain.customer.repository;

import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.example.moki_campaign.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 특정 매장에 속한 모든 고객 조회
    List<Customer> findAllByStore(Store store);

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