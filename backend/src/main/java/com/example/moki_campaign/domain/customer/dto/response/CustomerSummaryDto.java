package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "고객 요약 정보")
public class CustomerSummaryDto {

    @Schema(description = "고객 ID", example = "1")
    @JsonProperty("customer_id")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String name;

    @Schema(description = "마지막 방문 이후 경과일", example = "5")
    @JsonProperty("visit_day_ago")
    private Integer visitDayAgo;

    @Schema(description = "총 방문 횟수", example = "23")
    @JsonProperty("total_visit_count")
    private Integer totalVisitCount;

    @Schema(description = "충성도 점수", example = "85")
    @JsonProperty("loyalty_score")
    private Integer loyaltyScore;
}
