package com.example.moki_campaign.global.auth;

import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.repository.StoreRepository;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.global.exception.dto.ErrorResponseDto;
import com.example.moki_campaign.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private static final List<String> WHITELIST_PREFIXES = List.of(
            "/api/auth",
            "/swagger-ui",
            "/swagger-resources",
            "/api-docs",
            "/v3/api-docs",
            "/actuator/health",
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 화이트리스트 체크
        if (isWhitelisted(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = request.getHeader(AUTH_HEADER);
            
            // Authorization 헤더가 없거나 비어있는 경우
            if (header == null || header.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // Bearer 접두사 체크
            if (!header.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 추출
            String token = header.substring(BEARER_PREFIX.length());

            // 토큰 검증
            jwtUtil.validate(token);

            // 토큰에서 사업자번호 추출
            String businessNumber = jwtUtil.getBusinessNumber(token);

            // DB에서 Store 조회
            Store store = storeRepository.findByBusinessNumber(businessNumber)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

            // SecurityContext에 인증 정보 저장
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    store, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 성공 - businessNumber: {}, storeId: {}", businessNumber, store.getId());

            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            log.warn("JWT 인증 실패 - BusinessException: {}", e.getMessage());
            writeUnauthorizedResponse(response, e.getErrorCode(), e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            log.error("JWT 인증 중 예외 발생", e);
            writeUnauthorizedResponse(response, ErrorCode.TOKEN_INVALID,
                    ErrorCode.TOKEN_INVALID.message, request.getRequestURI());
        }
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return WHITELIST_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, ErrorCode errorCode,
                                           String message, String path) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(errorCode.status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");

        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                errorCode, message, Collections.emptyList(), path
        );

        String body = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(body);
    }
}
