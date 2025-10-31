package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeclinedLoyalSummaryResponseDto(
        @Schema(description = "감소하는 단골 수", example = "7")
        @JsonProperty("decline_count")
        Integer declineCount,

        @Schema(description = "감소하는 단골 비율", example = "35")
        @JsonProperty("decline_ratio")
        Integer declineRatio
) {}
