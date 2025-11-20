package com.example.moki_campaign.domain.customer.controller;

import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.VisitGraphResponseDto;
import com.example.moki_campaign.domain.customer.service.CustomerService;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.global.auth.CurrentStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "고객 CRM", description = "고객 관리 API (전체/충성/이탈 고객)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/customers")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
            summary = "고객 CRM 목록 조회",
            description = """
                    전체, 충성, 이탈 고객, 충성 이탈 고객을 조건에 맞게 가져옵니다.
                    - segment=all: 전체 고객 (최근 방문일 순)
                    - segment=loyal: LOYAL + AT_RISK_LOYAL 고객 (충성도 점수 순)
                    - segment=risk_at_loyal: AT_RISK_LOYAL 고객만 (충성도 점수 순)
                    - segment=churn_risk: CHURN_RISK 고객 (충성도 점수 순)
                    - 메인 대시보드: size=5
                    - 고객 CRM 페이지: size=20 (무한 스크롤)
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Parameters({
            @Parameter(name = "segment", description = "조회할 고객 타입 [all, loyal, churn_risk, at_risk_loyal]", required = true, example = "all"),
            @Parameter(name = "size", description = "페이지 당 사이즈 (메인은 5, CRM은 20)", example = "20"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
    })
    @GetMapping
    public ResponseEntity<CustomerListResponseDto> getCustomers(
            @RequestParam String segment,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(hidden = true) @CurrentStore Store store
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CustomerListResponseDto response = customerService.findCustomerList(store, segment, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 상세 정보 조회",
            description = "특정 고객의 상세 정보 (기본 정보, 방문 이력, 월별 방문 빈도)를 조회합니다. 최근 6개월 (현재 달 포함)의 월별 방문 횟수를 이전 달부터 순서대로 제공합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{customerId}")
    @Parameters({
            @Parameter(name = "customerId", description = "고객 ID", required = true, example = "1")
    })
    public ResponseEntity<CustomerDetailResponseDto> getCustomerDetail(
            @PathVariable Long customerId,
            @Parameter(hidden = true) @CurrentStore Store store
    ) {

        CustomerDetailResponseDto response = customerService.findCustomerDetail(store, customerId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 방문 빈도 그래프 조회",
            description = """
                    특정 고객의 방문 빈도 그래프 데이터를 조회합니다.
                    - period=month: 최근 6개월 월별 방문 횟수 (라벨: yyyy-MM)
                    - period=week: 최근 8주 주별 방문 횟수 (라벨: yyyy-MM-dd, 월요일 기준)
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{customerId}/graph")
    @Parameters({
            @Parameter(name = "customerId", description = "고객 ID", required = true, example = "1"),
            @Parameter(name = "period", description = "조회 기간 단위 [week, month]", required = true, example = "month")
    })
    public ResponseEntity<VisitGraphResponseDto> getCustomerVisitGraph(
            @PathVariable Long customerId,
            @RequestParam String period,
            @Parameter(hidden = true) @CurrentStore Store store
    ) {

        VisitGraphResponseDto response = customerService.findCustomerVisitGraph(store, customerId, period);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "이탈 위험 단골 정보 조회",
            description = """
                    이탈 위험이 있는 단골 고객(AT_RISK_LOYAL)의 수와 전체 단골 고객 중 비율을 조회합니다.
                    - decline_count: AT_RISK_LOYAL 고객 수
                    - decline_ratio: (AT_RISK_LOYAL / (LOYAL + AT_RISK_LOYAL)) * 100 (백분율)
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/decline")
    public ResponseEntity<DeclinedLoyalSummaryResponseDto> getDecliningCustomers(
            @Parameter(hidden = true) @CurrentStore Store store
    ) {
        DeclinedLoyalSummaryResponseDto response = customerService.findDeclinedLoyalInfo(store);

        return ResponseEntity.ok(response);
    }
}
