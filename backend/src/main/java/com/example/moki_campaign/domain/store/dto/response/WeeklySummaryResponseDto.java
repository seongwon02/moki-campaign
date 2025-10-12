package com.example.moki_campaign.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "이번주 요약 정보")
public class WeeklySummaryResponseDto {

    @Schema(description = "시작 날짜", example = "2025-10-06")
    @JsonProperty("start_date")
    private String startDate;

    @Schema(description = "종료 날짜", example = "2025-10-12")
    @JsonProperty("end_date")
    private String endDate;

    @Schema(description = "총 매출", example = "3500000")
    @JsonProperty("total_sales")
    private Long totalSales;

    @Schema(description = "매출 변화량", example = "200000")
    @JsonProperty("sales_change")
    private Long salesChange;

    @Schema(description = "방문 고객 수", example = "156")
    @JsonProperty("visited_customer_count")
    private Integer visitedCustomerCount;

    @Schema(description = "고객 수 변화량", example = "12")
    @JsonProperty("customer_count_change")
    private Integer customerCountChange;

    @Schema(description = "재방문율", example = "0.64")
    @JsonProperty("revisit_rate")
    private Double revisitRate;

    @Schema(description = "재방문율 변화량", example = "0.03")
    @JsonProperty("revisit_rate_change")
    private Double revisitRateChange;
}
