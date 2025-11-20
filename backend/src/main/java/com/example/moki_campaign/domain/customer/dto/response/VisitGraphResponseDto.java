package com.example.moki_campaign.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "고객 방문 빈도 그래프 응답")
public record VisitGraphResponseDto(

    @Schema(description = "그래프 데이터 (월 단위: 6개, 주 단위: 8개)")
    List<VisitGraphItemDto> graph
) {}
