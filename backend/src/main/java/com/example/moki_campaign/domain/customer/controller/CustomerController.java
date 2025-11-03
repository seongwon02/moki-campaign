package com.example.moki_campaign.domain.customer.controller;

import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponse;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponse;
import com.example.moki_campaign.domain.customer.dto.response.CustomerSummaryDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "고객 CRM", description = "고객 관리 API (전체/충성/이탈 고객)")
@RestController
@RequestMapping("/api/stores/{storeId}/customers")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    @Operation(
            summary = "고객 CRM 목록 조회",
            description = """
                    전체, 충성, 이탈 고객, 충성 이탈 고객을 조건에 맞게 가져옵니다.
                    - 메인 대시보드: size=5
                    - 고객 CRM 페이지: size=20 (무한 스크롤)
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Parameters({
            @Parameter(name = "segment", description = "조회할 고객 타입 [all, loyal, churn_risk, risk_at_loyal]", required = true, example = "all"),
            @Parameter(name = "size", description = "페이지 당 사이즈 (메인은 5, CRM은 20)", example = "5"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
    })
    @GetMapping
    public ResponseEntity<CustomerListResponse> getCustomers(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @RequestParam String segment,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "0") Integer page
    ) {
        // TODO: 실제 고객 목록 조회 로직 구현
        List<CustomerSummaryDto> customers = List.of(
                CustomerSummaryDto.builder()
                        .customerId(1L)
                        .name("홍길동")
                        .visitDayAgo(5)
                        .totalVisitCount(23)
                        .loyaltyScore(85)
                        .build(),
                CustomerSummaryDto.builder()
                        .customerId(2L)
                        .name("김철수")
                        .visitDayAgo(10)
                        .totalVisitCount(15)
                        .loyaltyScore(72)
                        .build()
        );

        CustomerListResponse response = CustomerListResponse.builder()
                .customers(customers)
                .size(size)
                .page(page)
                .hasNext(true)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 상세 정보 조회",
            description = "특정 고객의 상세 정보 (기본 정보, 방문 이력, 월별 방문 빈도)를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{customerId}")
    @Parameters({
            @Parameter(description = "매장 ID", required = true, example = "1"),
            @Parameter(description = "고객 ID", required = true, example = "1")
    })
    public ResponseEntity<CustomerDetailResponse> getCustomerDetail(
            @PathVariable Long storeId,
            @PathVariable Long customerId
    ) {
        // TODO: 실제 고객 상세 정보 조회 로직 구현
        CustomerDetailResponse response = CustomerDetailResponse.builder()
                .customerId(1L)
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .totalSpent(500000L)
                .loyaltyScore(85)
                .churnRiskLevel("LOW")
                .currentPoints(1500)
                .visit(CustomerDetailResponse.VisitInfo.builder()
                        .totalVisitCount(23)
                        .visitDayAgo(5)
                        .build())
                .analytics(CustomerDetailResponse.AnalyticsInfo.builder()
                        .visitFrequency(List.of(
                                CustomerDetailResponse.MonthlyVisit.builder()
                                        .month("2025-09")
                                        .count(5)
                                        .build(),
                                CustomerDetailResponse.MonthlyVisit.builder()
                                        .month("2025-08")
                                        .count(8)
                                        .build(),
                                CustomerDetailResponse.MonthlyVisit.builder()
                                        .month("2025-07")
                                        .count(10)
                                        .build()
                        ))
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "방문 감소 정보 조회",
            description = """
                    방문 감소 요약 정보와 해당하는 고객 목록을 가져옵니다.
                    고객 목록은 무한 스크롤로 구현됩니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Parameters({
            @Parameter(description = "매장 ID", required = true, example = "1"),
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(description = "페이지 크기", example = "20")
    })
    @GetMapping("/decline")
    public ResponseEntity<DeclinedLoyalSummaryResponseDto> getDecliningCustomers(
            @PathVariable Long storeId
    ) {
        // TODO: 실제 방문 감소 고객 조회 로직 구현
        DeclinedLoyalSummaryResponseDto response = new DeclinedLoyalSummaryResponseDto(
                10,
                35
        );

        return ResponseEntity.ok(response);
    }
}
