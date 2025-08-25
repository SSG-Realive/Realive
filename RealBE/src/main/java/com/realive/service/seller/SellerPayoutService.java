package com.realive.service.seller;

import com.realive.domain.logs.SalesLog;
import com.realive.domain.order.OrderItem;
import com.realive.dto.logs.CommissionLogDTO;
import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.PayoutLogDetailDTO;
import com.realive.dto.logs.SalesLogDTO;
import com.realive.dto.seller.SellerPayoutSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SellerPayoutService {
    List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId);

    // 페이징 메서드 추가
    Page<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId, Pageable pageable);

    List<PayoutLogDTO> getPayoutLogsByDate(Long sellerId, LocalDate date);

    void generatePayoutLogIfNotExists(Long orderId);

    PayoutLogDetailDTO getPayoutLogDetail(Long sellerId, Integer payoutLogId);

    List<PayoutLogDTO> getPayoutLogsByPeriod(Long sellerId, LocalDate from, LocalDate to);

    SellerPayoutSummaryDTO getPayoutSummary(Long sellerId, LocalDate from, LocalDate to);

    SalesLog createSalesLog(OrderItem orderItem);           // 판매 로그 생성
    void createCommissionLog(Integer salesLogId);       // 수수료 로그 생성
    List<SalesLogDTO> getSalesLogsBySeller(Long sellerId, LocalDate from, LocalDate to);
    List<CommissionLogDTO> getCommissionLogsBySeller(Long sellerId, LocalDate from, LocalDate to);
}
