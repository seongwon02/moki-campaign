package com.example.moki_campaign.domain.store.controller;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import com.example.moki_campaign.domain.store.service.StoreService;
import com.example.moki_campaign.global.auth.CurrentStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "메인 대시보드", description = "메인 대시보드 요약 정보 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "이번주 요약 조회",
            description = "이번주 매출, 방문 고객 수, 재방문율 등의 요약 정보를 가져옵니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/main/weekly")
    public ResponseEntity<WeeklySummaryResponseDto> getWeeklySummary(
            @Parameter(hidden = true) @CurrentStore Store currentStore
    ) {
        // JWT에서 추출한 currentStore를 사용하여 주간 요약 조회
        WeeklySummaryResponseDto response = storeService.findWeeklySummary(currentStore);
        return ResponseEntity.ok(response);
    }
}
