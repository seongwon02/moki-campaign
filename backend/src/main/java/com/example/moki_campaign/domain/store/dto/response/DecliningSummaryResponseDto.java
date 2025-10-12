package com.example.moki_campaign.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "방문 감소 충성 고객 요약")
public class DecliningSummaryResponseDto {

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
