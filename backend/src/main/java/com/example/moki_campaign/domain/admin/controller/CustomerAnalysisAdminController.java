package com.example.moki_campaign.domain.admin.controller;

import com.example.moki_campaign.domain.customer.service.CustomerService;
import com.example.moki_campaign.domain.visit.service.DailyVisitCreationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 테스트용 API 컨트롤러
@Tag(name = "테스트", description = "고객분석 테스트 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerAnalysisAdminController {

    private final CustomerService customerService;
    private final DailyVisitCreationService dailyVisitCreationService; // 랜덤 방문 생성 서비스

    @Operation(summary = "AI 고객 분석 수동 실행 (비동기)",
            description = "스케줄러를 기다리지 않고 AI 고객 분석(analyzeAllStores)을 즉시 비동기로 실행합니다.")
    @ApiResponse(responseCode = "202", description = "분석 작업이 성공적으로 시작됨 (비동기)")
    @ApiResponse(responseCode = "500", description = "작업 시작 중 오류 발생")
    @PostMapping("/customers/run-analysis")
    public ResponseEntity<String> manuallyRunAnalysis() {
        log.warn("========= 수동 AI 고객 분석 실행 요청 (Admin) =========");
        try {
            // @Async가 적용된 analyzeAllStores 호출
            customerService.analyzeAllStores();

            String responseMessage = "AI 고객 분석 작업이 비동기로 시작되었습니다. (완료까지 시간이 걸릴 수 있습니다)";

            // 202 Accepted: 요청은 접수했으나, 처리가 완료되지 않음 (비동기)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseMessage);

        } catch (Exception e) {
            log.error("수동 AI 고객 분석 실행 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("분석 작업 시작 중 오류 발생: " + e.getMessage());
        }
    }

}