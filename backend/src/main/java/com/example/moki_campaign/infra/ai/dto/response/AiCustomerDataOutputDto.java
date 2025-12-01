package com.example.moki_campaign.infra.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiCustomerDataOutputDto (
        @JsonProperty("customer_id")
        String customerId,

        @JsonProperty("customer_segment")
        String customerSegment,

        @JsonProperty("predicted_loyalty_score")
        Double predictedLoyaltyScore
){}
