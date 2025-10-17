package com.example.moki_campaign.domain.auth.controller;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import com.example.moki_campaign.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사업자번호와 비밀번호로 로그인합니다.")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto response = authService.login(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
