package com.example.moki_campaign.auth;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import com.example.moki_campaign.domain.auth.service.AuthService;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.global.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto loginRequest;
    private Store existingStore;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDto("1234567890", "password123");

        existingStore = Store.builder()
                .businessNumber("1234567890")
                .password("$2a$10$encodedPassword")
                .name("테스트 매장")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Test
    void 기존_매장_로그인_성공() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.of(existingStore));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        given(jwtUtil.generateAccessToken(anyString(), anyString()))
                .willReturn("generated.jwt.token");

        // when
        LoginResponseDto response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("generated.jwt.token");

        verify(storeRepository).findByBusinessNumber("1234567890");
        verify(passwordEncoder).matches("password123", existingStore.getPassword());
        verify(jwtUtil).generateAccessToken("1234567890", "테스트 매장");
    }

    @Test
    void 비밀번호_불일치로_인한_로그인_실패() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.of(existingStore));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(passwordEncoder).matches("password123", existingStore.getPassword());
        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void 존재하지_않는_매장_로그인_실패() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(storeRepository).findByBusinessNumber("1234567890");
        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
    }
}
