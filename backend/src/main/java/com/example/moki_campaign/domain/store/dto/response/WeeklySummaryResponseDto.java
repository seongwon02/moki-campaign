package com.example.moki_campaign.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "이번주 요약 정보")
public record WeeklySummaryResponseDto(
        @Schema(description = "가게 이름", example = "메스 커피")
        @JsonProperty("store_name")
        String storeName,

        @Schema(description = "시작 날짜", example = "2025-10-06")
        @JsonProperty("start_date")
        String startDate,

        @Schema(description = "종료 날짜", example = "2025-10-12")
        @JsonProperty("end_date")
        String endDate,

        @Schema(description = "총 매출", example = "3500000")
        @JsonProperty("total_sales")
        Long totalSales,

        @Schema(description = "매출 변화량", example = "200000")
        @JsonProperty("sales_change")
        Long salesChange,

        @Schema(description = "방문 고객 수", example = "156")
        @JsonProperty("visited_customer_count")
        Integer visitedCustomerCount,

        @Schema(description = "고객 수 변화량", example = "12")
        @JsonProperty("customer_count_change")
        Integer customerCountChange,

        @Schema(description = "재방문율", example = "64")
        @JsonProperty("revisit_rate")
        Integer revisitRate,

        @Schema(description = "재방문율 변화량", example = "3")
        @JsonProperty("revisit_rate_change")
        Integer revisitRateChange
) {


}
