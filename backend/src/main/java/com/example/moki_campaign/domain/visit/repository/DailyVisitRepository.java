package com.example.moki_campaign.domain.visit.repository;

import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface DailyVisitRepository extends JpaRepository<DailyVisit, Long> {

    // 특정 기간동안 매장에 방문한 기록 데이터 조회
    @Query("SELECT dv FROM DailyVisit dv JOIN FETCH dv.customer " +
            "WHERE dv.store = :store AND dv.visitDate BETWEEN :startDate AND :endDate")
    List<DailyVisit> findByStoreAndDateRangeWithCustomer(@Param("store") Store store,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    // 지정된 기간과 매장의 총 매출 합산
    @Query("SELECT COALESCE(SUM(dv.amount), 0L) " +
            "FROM DailyVisit dv " +
            "WHERE dv.store = :store AND dv.visitDate BETWEEN :startDate AND :endDate")
    Long getSalesByDateRange(@Param("store") Store store,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    // 지정된 기간과 매장의 방문 고객 수 계산
    @Query("SELECT COUNT(DISTINCT dv.customer.id) " +
            "FROM DailyVisit dv " +
            "WHERE dv.store = :store AND dv.visitDate BETWEEN :startDate AND :endDate")
    Integer getVisitorCountByDateRange(@Param("store") Store store,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);


    // 지정된 기간과 매장의 방문한 고유 고객 ID 목록(Set)을 반환
    @Query("SELECT DISTINCT dv.customer.id " +
            "FROM DailyVisit dv " +
            "WHERE dv.store = :store AND dv.visitDate BETWEEN :startDate AND :endDate")
    Set<Long> getVisitorIdsByDateRange(@Param("store") Store store,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // 특정 고객의 지정된 기간 동안의 방문 기록 조회
    @Query("SELECT dv FROM DailyVisit dv " +
            "WHERE dv.customer.id = :customerId AND dv.visitDate BETWEEN :startDate AND :endDate " +
            "ORDER BY dv.visitDate ASC")
    List<DailyVisit> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}