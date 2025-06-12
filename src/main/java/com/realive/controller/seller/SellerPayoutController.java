package com.realive.controller.seller;

import com.realive.domain.seller.Seller;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/settlements")
public class SellerPayoutController {

    private final SellerPayoutService sellerPayoutService;

    // 🔍 판매자의 정산 내역 조회
    @GetMapping
    public List<PayoutLogDTO> getMyPayoutLogs() {
        Seller seller = (Seller) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return sellerPayoutService.getPayoutLogsBySellerId(seller.getId());
    }
}
