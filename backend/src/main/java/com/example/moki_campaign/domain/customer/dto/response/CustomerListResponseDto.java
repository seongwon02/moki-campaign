package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "고객 목록 응답")
public record CustomerListResponseDto (

    @Schema(description = "고객 목록")
    List<CustomerSummaryDto> customers,

    @Schema(description = "페이지 크기", example = "20")
    Integer size,

    @Schema(description = "현재 페이지 번호", example = "0")
    Integer page,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    @JsonProperty("has_next")
    Boolean hasNext
){}
