package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record AnalyticsReponseDto(
        @Schema(description = "월 (yyyy-MM)", example = "2025-05")
        @JsonProperty("month")
        String month,

        @Schema(description = "방문 횟수", example = "5")
        @JsonProperty("count")
        Integer count
) {}