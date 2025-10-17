package com.example.moki_campaign.infra.moki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MokiLoginResponseDto(
        @JsonProperty("result")
        String result,

        @JsonProperty("msg")
        String msg,

        @JsonProperty("mb_id")
        String mbId,

        @JsonProperty("mb_name")
        String mbName,

        @JsonProperty("mb_hp")
        String mbHp,

        @JsonProperty("mb_email")
        String mbEmail,

        @JsonProperty("mb_addr")
        String mbAddr,

        @JsonProperty("waiting_use")
        String waitingUse,

        @JsonProperty("pos_use")
        String posUse,

        @JsonProperty("mb_kiosk_type")
        String mbKioskType,

        @JsonProperty("banner_image")
        String bannerImage,

        @JsonProperty("used_banner")
        String usedBanner,

        @JsonProperty("mb_newop")
        String mbNewop
) {
    public boolean isSuccess() {
        return "Y".equals(result);
    }
}
