package com.example.moki_campaign.domain.admin.dto;

import com.example.moki_campaign.domain.customer.entity.CustomerSegment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 수동으로 특정 고객을 생성하기 위한 DTO
 * 운영 단계에서 admin 삭제 예정
 */
@Getter
@NoArgsConstructor
public class CustomerSeedRequestDto {

    // Customer 엔티티의 필드를 그대로 따릅니다.
    // storeId는 필수입니다.

    @JsonProperty("store_id")
    private Long storeId;

    private String name;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("total_amount")
    private Integer totalAmount;

    private Integer points;

    private CustomerSegment segment;

    @JsonProperty("loyalty_score")
    private Integer loyaltyScore;

    @JsonProperty("total_visit_count")
    private Integer totalVisitCount;

    @JsonProperty("last_visit_date")
    private LocalDate lastVisitDate;
}