package com.example.moki_campaign.infra.ai.dto.request;

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

        @JsonProperty("visits_8_week_ago")
        Integer visits8WeekAgo,

        @JsonProperty("visits_7_week_ago")
        Integer visits7WeekAgo,

        @JsonProperty("visits_6_week_ago")
        Integer visits6WeekAgo,

        @JsonProperty("visits_5_week_ago")
        Integer visits5WeekAgo,

        @JsonProperty("visits_4_week_ago")
        Integer visits4WeekAgo,

        @JsonProperty("visits_3_week_ago")
        Integer visits3WeekAgo,

        @JsonProperty("visits_2_week_ago")
        Integer visits2WeekAgo,

        @JsonProperty("visits_1_week_ago")
        Integer visits1WeekAgo
) {
}
