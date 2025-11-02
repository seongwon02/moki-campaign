package com.example.moki_campaign.infra.moki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MokiSalesResponseDto(
        @JsonProperty("total_revenue")
        Long totalRevenue,

        @JsonProperty("total_count")
        Integer totalCount,

        @JsonProperty("data")
        List<SalesData> data
) {
    public record SalesData(
            @JsonProperty("menu_id")
            Long menuId,

            @JsonProperty("menu_name")
            String menuName,

            @JsonProperty("date")
            String date,

            @JsonProperty("hour")
            String hour,

            @JsonProperty("count")
            String count,

            @JsonProperty("revenue")
            String revenue
    ) {}
}
