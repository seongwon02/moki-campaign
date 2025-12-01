package com.example.moki_campaign.infra.ai.client;

import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import com.example.moki_campaign.infra.ai.dto.request.AiAnalysisRequestDto; // [추가]
import com.example.moki_campaign.infra.ai.dto.request.AiCustomerDataInputDto;
import com.example.moki_campaign.infra.ai.dto.response.AiCustomerDataResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;

import java.util.List;

@Component
@Slf4j
public class AiClient {
    private final RestClient restClient;

    public AiClient(
            @Value("${ai.service.url}") String baseUrl)
    {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMinutes(5));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    log.error("AI 서버 에러 - Status: {}, Body: {}", response.getStatusCode(), response.getBody().toString());
                    throw new BusinessException(ErrorCode.AI_SERVER_CONNECT_ERROR);
                })
                .build();
    }

    public AiCustomerDataResponseDto analyzeCustomers(List<AiCustomerDataInputDto> customerData) {
        try {
            log.info("AI 서버 고객 분석 요청. 고객 수: {}", customerData.size());

            AiAnalysisRequestDto requestBody = new AiAnalysisRequestDto(customerData);

            AiCustomerDataResponseDto response = restClient.post()
                    .uri("/api/ai/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(AiCustomerDataResponseDto.class);

            if (response == null || response.result() == null) {
                log.error("AI 서버 응답이 비어있습니다.");
                throw new BusinessException(ErrorCode.INVAILD_AI_SERVER_RESPONSE);
            }

            log.info("AI 서버 고객 분석 완료. 응답 수: {}", response.result().size());
            return response;

        } catch (Exception e) {
            log.error("AI 서버 통신 중 예외 발생", e);
            throw new BusinessException(ErrorCode.AI_SERVER_CONNECT_ERROR);
        }
    }
}