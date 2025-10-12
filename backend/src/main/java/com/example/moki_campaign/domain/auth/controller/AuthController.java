package com.example.moki_campaign.domain.auth.controller;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "로그인 및 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(
            summary = "로그인",
            description = "사업자 번호와 비밀번호를 입력해 로그인을 요청합니다. 입력 정보가 맞으면 인증 토큰(JWT)을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        // TODO: 실제 로그인 로직 구현
        return ResponseEntity.ok(new LoginResponseDto("sample-jwt-token"));
    }
}
