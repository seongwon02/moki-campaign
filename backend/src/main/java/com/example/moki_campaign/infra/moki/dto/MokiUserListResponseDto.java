package com.example.moki_campaign.infra.moki.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record MokiUserListResponseDto(
        @JsonProperty("data")
        List<UserData> data
) {
    public record UserData(
            @JsonProperty("user_name")
            String userName,

            @JsonProperty("phone_num")
            String phoneNum,

            @JsonProperty("total_point")
            String totalPoint,

            @JsonProperty("visit_count")
            String visitCount,

            @JsonProperty("total_count")
            Integer totalCount,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("first_visit")
            LocalDateTime firstVisit
    ) {}
}
