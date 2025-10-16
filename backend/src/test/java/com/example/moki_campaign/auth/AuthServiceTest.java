package com.example.moki_campaign.auth;

import com.example.moki_campaign.domain.auth.dto.request.LoginRequestDto;
import com.example.moki_campaign.domain.auth.dto.response.LoginResponseDto;
import com.example.moki_campaign.domain.auth.service.AuthService;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.global.util.JwtUtil;
import com.example.moki_campaign.infra.moki.client.MokiClient;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
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
    private MokiClient mokiClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto loginRequest;
    private Store existingStore;
    private MokiLoginResponseDto mokiSuccessResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDto("1234567890", "password123");

        existingStore = Store.builder()
                .businessNumber("1234567890")
                .password("$2a$10$encodedPassword")
                .name("테스트 매장")
                .phoneNumber("010-1234-5678")
                .build();

        mokiSuccessResponse = new MokiLoginResponseDto(
                "Y",
                "로그인 되었습니다.",
                "1234567890",
                "테스트 매장",
                "010-1234-5678",
                "test@example.com",
                "서울시 강남구",
                "1",
                "1",
                "old",
                "https://example.com/banner.jpg",
                "Y",
                "Y"
        );
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
        verify(mokiClient, never()).login(anyString(), anyString());
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void 비밀번호_불일치로_인한_기존_매장_로그인_실패() {
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
        verify(mokiClient, never()).login(anyString(), anyString());
    }

    @Test
    void 신규_매장_로그인_성공() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.empty());
        given(mokiClient.login(anyString(), anyString()))
                .willReturn(mokiSuccessResponse);
        given(passwordEncoder.encode(anyString()))
                .willReturn("$2a$10$encodedPassword");
        given(jwtUtil.generateAccessToken(anyString(), anyString()))
                .willReturn("generated.jwt.token");

        // when
        LoginResponseDto response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("generated.jwt.token");

        verify(storeRepository).findByBusinessNumber("1234567890");
        verify(mokiClient).login("1234567890", "password123");
        verify(passwordEncoder).encode("password123");
        verify(storeRepository).save(argThat(store ->
                store.getBusinessNumber().equals("1234567890") &&
                store.getName().equals("테스트 매장") &&
                store.getPhoneNumber().equals("010-1234-5678")
        ));
        verify(jwtUtil).generateAccessToken("1234567890", "테스트 매장");
    }

    @Test
    void 모키서버_인증실패로_인한_신규_매장_로그인_실패() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.empty());
        given(mokiClient.login(anyString(), anyString()))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "모키 인증 실패"));

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(storeRepository).findByBusinessNumber("1234567890");
        verify(mokiClient).login("1234567890", "password123");
        verify(storeRepository, never()).save(any(Store.class));
        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void 모키_서버_연결_실패로_인한_신규_매장_로그인_실패() {
        // given
        given(storeRepository.findByBusinessNumber(anyString()))
                .willReturn(Optional.empty());
        given(mokiClient.login(anyString(), anyString()))
                .willThrow(new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 서버 연결 실패"));

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_AUTH_FAILED);

        verify(mokiClient).login("1234567890", "password123");
        verify(storeRepository, never()).save(any(Store.class));
    }
}
