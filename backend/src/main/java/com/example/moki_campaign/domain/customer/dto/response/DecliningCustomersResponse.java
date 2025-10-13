package com.example.moki_campaign.domain.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "방문 감소 정보 응답")
public class DecliningCustomersResponse {

    @Schema(description = "요약 정보")
    private Summary summary;

    @Schema(description = "고객 목록")
    private CustomerPage customers;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "감소 요약 정보")
    public static class Summary {

        @Schema(description = "감소하는 충성 고객 수", example = "18")
        @JsonProperty("decline_count")
        private Integer declineCount;

        @Schema(description = "이전 평균 방문 간격 (일)", example = "5")
        @JsonProperty("prev_visit_intv")
        private Integer prevVisitIntv;

        @Schema(description = "현재 평균 방문 간격 (일)", example = "2")
        @JsonProperty("cur_visit_intv")
        private Integer curVisitIntv;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "고객 페이지 정보")
    public static class CustomerPage {

        @Schema(description = "고객 목록")
        private java.util.List<CustomerSummaryDto> content;

        @Schema(description = "현재 페이지 번호", example = "0")
        private Integer page;

        @Schema(description = "페이지 크기", example = "20")
        private Integer size;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;
    }
}
