package com.example.moki_campaign.domain.auth.service;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final StoreRepository storeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        String businessNumber = request.businessNumber();
        String password = request.password();

        Store store = storeRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS)); // 로그인 실패

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, store.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
            
        String token = jwtUtil.generateAccessToken(businessNumber, store.getName());
        return new LoginResponseDto(token);
    }
}
