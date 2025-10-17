package com.example.moki_campaign.domain.auth.service;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.util.JwtUtil;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final StoreRepository storeRepository;
    private final JwtUtil jwtUtil;
    private final MokiClient mokiClient;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        String businessNumber = request.businessNumber();
        String password = request.password();

        Optional<Store> existingStore = storeRepository.findByBusinessNumber(businessNumber);

        // DB에 Store가 있으면 비밀번호 확인 후 토큰 발급
        if (existingStore.isPresent()) {
            Store store = existingStore.get();
            
            // 비밀번호 검증
            if (!passwordEncoder.matches(password, store.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            
            String token = jwtUtil.generateAccessToken(businessNumber, store.getName());
            return new LoginResponseDto(token);
        }

        // DB에 Store가 없는 경우 모키 키오스크 서버를 통해 확인
        MokiLoginResponseDto mokiResponse = mokiClient.login(businessNumber, password);
        String encodedPassword = passwordEncoder.encode(password);
        
        Store newStore = Store.builder()
                .businessNumber(mokiResponse.mbId())
                .password(encodedPassword)
                .name(mokiResponse.mbName())
                .phoneNumber(mokiResponse.mbHp())
                .build();

        storeRepository.save(newStore);

        // 토큰 발급
        String token = jwtUtil.generateAccessToken(businessNumber, newStore.getName());
        return new LoginResponseDto(token);
    }
}
