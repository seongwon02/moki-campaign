package com.example.moki_campaign.domain.customer.entity;

import com.example.moki_campaign.domain.baestime.AuditingEntity;
import com.example.moki_campaign.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "customer", indexes = {
        @Index(name = "idx_customer_store_phone", columnList = "store_id, phone_number")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Customer extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false, length = 20)
    private CustomerSegment segment;

    @Column(name = "loyalty_score", nullable = false)
    private Integer loyaltyScore;

    @Column(name = "total_visit_count", nullable = false)
    private Integer totalVisitCount;

    @Column(name = "last_visit_date", nullable = false)
    private LocalDate lastVisitDate;

    @Builder
    public Customer(
            Store store, String name, String phoneNumber,
            Integer totalAmount, Integer points,
            CustomerSegment segment, Integer loyaltyScore,
            Integer totalVisitCount, LocalDate lastVisitDate)
    {
        this.store = store;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.totalAmount = totalAmount;
        this.points = points;
        this.segment = segment != null ? segment : CustomerSegment.GENERAL;
        this.loyaltyScore = loyaltyScore;
        this.totalVisitCount = totalVisitCount;
        this.lastVisitDate = lastVisitDate;
    }

}
