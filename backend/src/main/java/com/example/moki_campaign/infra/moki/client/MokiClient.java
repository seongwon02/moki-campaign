package com.example.moki_campaign.infra.moki.client;

import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.infra.moki.dto.MokiLoginResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiSalesResponseDto;
import com.example.moki_campaign.infra.moki.dto.MokiUserListResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class MokiClient {

    private static final String BASE_URL = "http://kioskmanager.co.kr/admin/api";
    private static final String SALES_BASE_URL = "http://mobilekiosk.co.kr/api";
    
    private final RestClient restClient;
    private final RestClient salesRestClient;

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

        // Sales API용 RestClient 생성 (간단한 에러 핸들링)
        this.salesRestClient = RestClient.builder()
                .baseUrl(SALES_BASE_URL)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    log.error("모키 매출 API 에러 - 상태코드: {}", response.getStatusCode());
                    throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 키오스크 매출 API 호출에 실패했습니다.");
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

            MokiLoginResponseDto response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/login.php")
                            .queryParam("mb_id", businessNumber) // 파라미터 이름: mb_id
                            .queryParam("mb_password", password) // 파라미터 이름: mb_password
                            .build())
                    .retrieve()
                    .body(MokiLoginResponseDto.class);

            if (response != null && response.isSuccess()) {
                log.info("모키 키오스크 로그인 성공 - 사업자번호: {}, 매장명: {}",
                        response.mbId(), response.mbName());
            } else {
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

    /**
     * 기간별 매출 조회
     * @param businessNumber 사업자번호 (mb_id)
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @return Optional<MokiSalesResponseDto>
     */
    public Optional<MokiSalesResponseDto> getSalesData(String businessNumber, String startDate, String endDate) {
        try {
            log.debug("모키 매출 조회 요청 - mb_id: {}, 기간: {} ~ {}", businessNumber, startDate, endDate);

            MokiSalesResponseDto response = salesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sale/weekly2.php")
                            .queryParam("mb_id", businessNumber)
                            .queryParam("start_date", startDate)
                            .queryParam("end_date", endDate)
                            .build())
                    .retrieve()
                    .body(MokiSalesResponseDto.class);

            if (response != null) {
                log.info("모키 매출 조회 성공 - 총 매출: {}, 건수: {}", 
                        response.totalRevenue(), response.totalCount());
                return Optional.of(response);
            } else {
                log.warn("모키 매출 조회 응답이 null입니다.");
                return Optional.empty();
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("모키 매출 API 호출 실패 - 사업자번호: {}, 에러: {}",
                    businessNumber, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 매출 데이터 조회에 실패했습니다.");
        }
    }

    /**
     * 특정 기간 동안 방문한 회원 리스트 조회
     * @param businessNumber 사업자번호 (mb_id)
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @return Optional<MokiUserListResponseDto>
     */
    public Optional<MokiUserListResponseDto> getUserList(String businessNumber, String startDate, String endDate) {
        try {
            log.debug("모키 회원 리스트 조회 요청 - mb_id: {}, 기간: {} ~ {}", businessNumber, startDate, endDate);

            MokiUserListResponseDto response = salesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/store/userlist.php")
                            .queryParam("mb_id", businessNumber)
                            .queryParam("start_date", startDate)
                            .queryParam("end_date", endDate)
                            .build())
                    .retrieve()
                    .body(MokiUserListResponseDto.class);

            if (response != null && response.data() != null) {
                log.info("모키 회원 리스트 조회 성공 - 회원 수: {}", response.data().size());
                return Optional.of(response);
            } else {
                log.warn("모키 회원 리스트 조회 응답이 null이거나 데이터가 없습니다.");
                return Optional.empty();
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("모키 회원 리스트 API 호출 실패 - 사업자번호: {}, 에러: {}",
                    businessNumber, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_AUTH_FAILED, "모키 회원 데이터 조회에 실패했습니다.");
        }
    }
}
