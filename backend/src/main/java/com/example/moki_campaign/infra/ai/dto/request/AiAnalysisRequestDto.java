package com.example.moki_campaign.infra.ai.dto.request;

import java.util.List;

public record AiAnalysisRequestDto(
        List<AiCustomerDataInputDto> data
) {
}