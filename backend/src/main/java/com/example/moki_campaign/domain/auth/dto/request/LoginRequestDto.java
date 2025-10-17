package com.example.moki_campaign.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequestDto (

    @Schema(description = "사업자 번호", example = "1234567890")
    @NotBlank(message = "사업자 번호는 필수입니다")
    @JsonProperty("business_number")
    String businessNumber,

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다")
    String password
){}
