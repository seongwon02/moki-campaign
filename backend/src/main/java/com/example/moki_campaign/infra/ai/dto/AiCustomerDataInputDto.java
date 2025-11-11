package com.example.moki_campaign.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AiCustomerDataInputDto(
        @JsonProperty("customer_id")
        String customerId,

        @JsonProperty("amount")
        Double amount,

        @JsonProperty("total_visits")
        Integer totalVisits,

        @JsonProperty("days_since_last_visit")
        Integer daysSinceLastVisit,

        @JsonProperty("visits_6_month_ago")
        Integer visits6MonthAgo,

        @JsonProperty("visits_5_month_ago")
        Integer visits5MonthAgo,

        @JsonProperty("visits_4_month_ago")
        Integer visits4MonthAgo,

        @JsonProperty("visits_3_month_ago")
        Integer visits3MonthAgo,

        @JsonProperty("visits_2_month_ago")
        Integer visits2MonthAgo,

        @JsonProperty("visits_1_month_ago")
        Integer visits1MonthAgo
) {
}
