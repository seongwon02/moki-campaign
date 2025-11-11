package com.example.moki_campaign.domain.visit.entity;

import com.example.moki_campaign.domain.baestime.CreatedAtEntity;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_visit", indexes = {
        @Index(name = "idx_daily_visit_customer_date", columnList = "customer_id, visit_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyVisit extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Builder
    public DailyVisit(Store store, Customer customer, LocalDate visitDate, Integer amount) {
        this.store = store;
        this.customer = customer;
        this.visitDate = visitDate;
        this.amount = amount;
    }
}
