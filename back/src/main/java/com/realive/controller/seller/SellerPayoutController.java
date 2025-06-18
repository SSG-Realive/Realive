package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/settlements")
public class SellerPayoutController {

    private final SellerPayoutService sellerPayoutService;

    // ✅ 전체 정산 내역 조회
    @GetMapping
    public List<PayoutLogDTO> getMyPayoutLogs() {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return sellerPayoutService.getPayoutLogsBySellerId(seller.getId());
    }

    // ✅ 특정 날짜 기준 정산 내역 필터링
    @GetMapping("/by-date")
    public List<PayoutLogDTO> getLogsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return sellerPayoutService.getPayoutLogsByDate(seller.getId(), date);
    }
}