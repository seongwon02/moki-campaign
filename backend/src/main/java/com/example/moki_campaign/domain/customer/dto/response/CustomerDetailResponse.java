package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "고객 상세 정보")
public class CustomerDetailResponse {

    @Schema(description = "고객 ID", example = "1")
    @JsonProperty("customer_id")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Schema(description = "총 사용 금액", example = "500000")
    @JsonProperty("total_spent")
    private Long totalSpent;

    @Schema(description = "충성도 점수", example = "85")
    @JsonProperty("loyalty_score")
    private Integer loyaltyScore;

    @Schema(description = "이탈 위험 수준 (HIGH, MEDIUM, LOW)", example = "LOW")
    @JsonProperty("churn_risk_level")
    private String churnRiskLevel;

    @Schema(description = "현재 보유 포인트", example = "1500")
    @JsonProperty("current_points")
    private Integer currentPoints;

    @Schema(description = "방문 정보")
    private VisitInfo visit;

    @Schema(description = "분석 데이터")
    private AnalyticsInfo analytics;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "방문 정보")
    public static class VisitInfo {

        @Schema(description = "총 방문 횟수", example = "23")
        @JsonProperty("total_visit_count")
        private Integer totalVisitCount;

        @Schema(description = "마지막 방문 이후 경과일", example = "5")
        @JsonProperty("visit_day_ago")
        private Integer visitDayAgo;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "분석 정보")
    public static class AnalyticsInfo {

        @Schema(description = "월별 방문 빈도")
        @JsonProperty("visit_frequency")
        private List<MonthlyVisit> visitFrequency;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "월별 방문 정보")
    public static class MonthlyVisit {

        @Schema(description = "월 (yyyy-MM)", example = "2025-09")
        private String month;

        @Schema(description = "방문 횟수", example = "5")
        private Integer count;
    }
}
