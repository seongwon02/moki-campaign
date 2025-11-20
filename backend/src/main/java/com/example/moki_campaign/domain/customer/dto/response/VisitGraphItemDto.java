package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방문 빈도 그래프 항목")
public record VisitGraphItemDto(

    @Schema(description = "라벨 (월: yyyy-MM, 주: yyyy-MM-dd)", example = "2025-01")
    String label,

    @Schema(description = "방문 횟수", example = "5")
    Integer count
) {}
