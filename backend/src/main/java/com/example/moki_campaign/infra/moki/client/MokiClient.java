package com.example.moki_campaign.infra.moki.client;

import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
@Slf4j
public class MokiClient {

    private static final String BASE_URL = "http://kioskmanager.co.kr/admin/api";
    
    private final RestClient restClient;

    public MokiClient(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    try {
                        JsonNode errorNode = objectMapper.readTree(response.getBody());
                        String errorMsg = errorNode.path("msg").asText("외부 인증 서버 오류");
                        log.error("모키 서버 4xx 에러: {}", errorMsg);
                        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, errorMsg);
                    } catch (Exception e) {
                        log.error("모키 서버 에러 파싱 실패", e);
                        throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED);
                    }
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("모키 서버 5xx 에러");
                    throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 키오스크 서버에 일시적인 문제가 발생했습니다.");
                })
                .build();
    }

    /**
     * 모키 키오스크 서버 로그인 요청
     * @param businessNumber 사업자번호
     * @param password 비밀번호
     * @return MokiLoginResponseDto
     */
    public MokiLoginResponseDto login(String businessNumber, String password) {
        try {
            log.debug("모키 키오스크 로그인 요청 - mb_id: {}", businessNumber);

            // 👇 [수정] POST 요청은 유지하되, Body 대신 URI에 파라미터를 추가합니다.
            MokiLoginResponseDto response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/login.php")
                            .queryParam("mb_id", businessNumber) // 파라미터 이름: mb_id
                            .queryParam("mb_password", password) // 파라미터 이름: mb_password
                            .build())
                    // Body가 없으므로 contentType과 body는 제거합니다.
                    .retrieve()
                    .body(MokiLoginResponseDto.class);

            if (response != null && response.isSuccess()) {
                log.info("모키 키오스크 로그인 성공 - 사업자번호: {}, 매장명: {}",
                        response.mbId(), response.mbName());
            } else {
                // ... (이하 동일)
                log.warn("모키 키오스크 로그인 실패 - 사업자번호: {}, 메시지: {}",
                        businessNumber, response != null ? response.msg() : "응답 없음");

                String errorMsg = response != null ? response.msg() : "인증에 실패했습니다.";
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, errorMsg);
            }

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("모키 키오스크 API 호출 실패 - 사업자번호: {}, 에러: {}",
                    businessNumber, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 키오스크 서버 연결에 실패했습니다.");
        }
    }

    // 향후 다른 모키 API 추가 예시:
    // public MokiCustomerResponseDto getCustomerInfo(String customerId) { ... }
    // public MokiOrderResponseDto createOrder(MokiOrderRequestDto request) { ... }
    // public List<MokiMenuResponseDto> getMenuList(String storeId) { ... }
}
