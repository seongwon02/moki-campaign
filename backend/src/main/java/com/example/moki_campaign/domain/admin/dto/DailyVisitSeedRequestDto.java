package com.example.moki_campaign.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyVisitSeedRequestDto {

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("visit_date")
    private LocalDate visitDate;

    private Integer amount;
}