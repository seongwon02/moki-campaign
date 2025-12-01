package com.example.moki_campaign.infra.ai.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiCustomerDataResponseDto(

        @JsonProperty("result")
        List<AiCustomerDataOutputDto> result
) {}
