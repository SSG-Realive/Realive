package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.PayoutLogDetailDTO;
import com.realive.dto.seller.SellerPayoutSummaryDTO;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/settlements")
public class SellerPayoutController {

    private final SellerPayoutService sellerPayoutService;

    // ✅ 전체 정산 내역 조회 (페이징 지원)
    @GetMapping
    public Object getMyPayoutLogs(
            @AuthenticationPrincipal SellerPrincipal principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page == null) {
            // 페이징 없이 전체 조회
            return sellerPayoutService.getPayoutLogsBySellerId(principal.getId());
        }
        // 페이징 처리된 조회
        return sellerPayoutService.getPayoutLogsBySellerId(
                principal.getId(),
                PageRequest.of(page, size)
        );
    }

    // ✅ 특정 날짜 기준 정산 내역 필터링
    @GetMapping("/by-date")
    public List<PayoutLogDTO> getLogsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        
        return sellerPayoutService.getPayoutLogsByDate(principal.getId(), date);
    }

    // ✅ 기간별 정산 내역 조회
    @GetMapping("/by-period")
    public List<PayoutLogDTO> getLogsByPeriod(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        return sellerPayoutService.getPayoutLogsByPeriod(principal.getId(), from, to);
    }

    // ✅ 정산 요약 정보 조회
    @GetMapping("/summary")
    public SellerPayoutSummaryDTO getPayoutSummary(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        return sellerPayoutService.getPayoutSummary(principal.getId(), from, to);
    }

    // ✅ 정산 상세 정보 조회
    @GetMapping("/{payoutLogId}/detail")
    public PayoutLogDetailDTO getPayoutLogDetail(
            @PathVariable Integer payoutLogId,
            @AuthenticationPrincipal SellerPrincipal principal
    ) {
        return sellerPayoutService.getPayoutLogDetail(principal.getId(), payoutLogId);
    }

    // ✅ 정산 로그 자동 생성 (주문 완료 시 호출)
    @PostMapping("/generate")
    public ResponseEntity<Void> generatePayoutLog(@RequestParam Long orderId) {
        sellerPayoutService.generatePayoutLogIfNotExists(orderId);
        return ResponseEntity.ok().build();
    }

}