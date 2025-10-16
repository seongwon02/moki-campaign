package com.example.moki_campaign.domain.store.entity;

import com.example.moki_campaign.domain.baestime.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Builder
    public Store(String businessNumber, String password, String name, String phoneNumber) {
        this.businessNumber = businessNumber;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
