package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "고객 상세 정보")
public record CustomerDetailResponseDto (

    @Schema(description = "고객 ID", example = "1")
    @JsonProperty("customer_id")
    Long customerId,

    @Schema(description = "고객 이름", example = "홍길동")
    String name,

    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone_number")
    String phoneNumber,

    @Schema(description = "총 사용 금액", example = "500000")
    @JsonProperty("total_spent")
    Long totalSpent,

    @Schema(description = "충성도 점수", example = "85")
    @JsonProperty("loyalty_score")
    Integer loyaltyScore,

    @Schema(description = "이탈 위험 수준 (HIGH, MEDIUM, LOW)", example = "LOW")
    @JsonProperty("churn_risk_level")
    String churnRiskLevel,

    @Schema(description = "고객 분류 (LOYAL, AT_RISK_LOYAL, GENERAL, CHURN_RISK)", example = "LOYAL")
    String segment,

    @Schema(description = "현재 보유 포인트", example = "1500")
    @JsonProperty("current_points")
    Integer currentPoints,

    @Schema(description = "총 방문 횟수", example = "23")
    @JsonProperty("total_visit_count")
    Integer totalVisitCount,

    @Schema(description = "총 방문 횟수", example = "100")
    @JsonProperty("visit_day_ago")
    Integer visitDayAgo
){}
