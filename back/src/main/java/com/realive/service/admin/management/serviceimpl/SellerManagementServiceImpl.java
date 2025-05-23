//package com.realive.service.admin.management.serviceimpl;
//
//import com.realive.domain.product.Product;
//import com.realive.domain.seller.Seller;
//import com.realive.dto.admin.management.ProductDTO;
//import com.realive.dto.admin.management.SellerDTO;
//import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO; // StatService DTO
//import com.realive.repository.product.ProductRepository;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.service.admin.logs.StatService; // StatService 주입
//import com.realive.service.admin.management.service.SellerManagementService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.persistence.criteria.Predicate;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class SellerManagementServiceImpl implements SellerManagementService {
//
//    private final SellerRepository sellerRepository;
//    private final ProductRepository productRepository;
//    private final StatService statService; // 판매자 매출 통계용
//
//    @Override
//    public Page<SellerDTO> getSellers(Pageable pageable) {
//        return sellerRepository.findAll(pageable).map(this::convertToSellerDTO);
//    }
//
//    @Override
//    public Page<SellerDTO> searchSellers(String keyword, Pageable pageable) {
//        if (keyword == null || keyword.trim().isEmpty()) return getSellers(pageable);
//        Specification<Seller> spec = (root, query, cb) ->
//                cb.or(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
//                        cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"));
//        return sellerRepository.findAll(spec, pageable).map(this::convertToSellerDTO);
//    }
//
//    @Override
//    public SellerDTO getSellerById(Integer sellerId) {
//        Seller seller = sellerRepository.findById(sellerId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + sellerId));
//        return convertToSellerDTO(seller);
//    }
//
//    @Override
//    @Transactional
//    public SellerDTO updateSellerStatus(Integer sellerId, String status) {
//        Seller seller = sellerRepository.findById(sellerId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + sellerId));
//        if ("APPROVED".equalsIgnoreCase(status)) {
//            seller.setApproved(true); seller.setActive(true);
//        } else if ("PENDING".equalsIgnoreCase(status)) {
//            seller.setApproved(false);
//        } else if ("ACTIVE".equalsIgnoreCase(status)) {
//            seller.setActive(true);
//        } else if ("INACTIVE".equalsIgnoreCase(status)) {
//            seller.setActive(false);
//        } else {
//            throw new IllegalArgumentException("유효하지 않은 판매자 상태값: " + status);
//        }
//        return convertToSellerDTO(sellerRepository.save(seller));
//    }
//
//    @Override
//    @Transactional
//    public SellerDTO updateSellerCommission(Integer sellerId, BigDecimal commission) {
//        Seller seller = sellerRepository.findById(sellerId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + sellerId));
//        if (commission == null || commission.compareTo(BigDecimal.ZERO) < 0 || commission.compareTo(new BigDecimal("100")) > 0) {
//            throw new IllegalArgumentException("커미션은 0~100 사이 값.");
//        }
//        // seller.setCommissionRate(commission); // Seller.commissionRate 필드 가정
//        log.warn("Seller commission update logic for seller ID {} needs to be implemented.", sellerId);
//        return convertToSellerDTO(sellerRepository.save(seller));
//    }
//
//    @Override
//    public Page<ProductDTO> getSellerProducts(Integer sellerId, Pageable pageable) {
//        return productRepository.findBySellerId(sellerId.longValue(), pageable)
//                .map(this::convertToProductDTO);
//    }
//
//    @Override
//    public Map<String, Object> getSellerSalesStatistics(Integer sellerId, LocalDate startDate, LocalDate endDate) {
//        log.info("판매자 매출 통계 (StatService 활용) - ID: {}, 기간: {} ~ {}", sellerId, startDate, endDate);
//        Seller seller = sellerRepository.findById(sellerId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + sellerId));
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("sellerId", seller.getId());
//        stats.put("sellerName", seller.getName());
//
//        // StatService를 사용하여 판매자별 월별 통계 집계 (예시)
//        // startDate와 endDate를 포함하는 모든 월에 대해 getSellerMonthlySalesSummary 호출
//        YearMonth startYM = YearMonth.from(startDate);
//        YearMonth endYM = YearMonth.from(endDate);
//        BigDecimal totalSalesForPeriod = BigDecimal.ZERO;
//        long totalOrdersForPeriod = 0L; // StatService의 DTO에 판매건수(주문단위)가 있다면 합산
//
//        for (YearMonth ym = startYM; !ym.isAfter(endYM); ym = ym.plusMonths(1)) {
//            MonthlySalesSummaryDTO monthlySummary = statService.getSellerMonthlySalesSummary(sellerId, ym);
//            if (monthlySummary != null && monthlySummary.getTotalSalesAmount() != null) {
//                totalSalesForPeriod = totalSalesForPeriod.add(BigDecimal.valueOf(monthlySummary.getTotalSalesAmount()));
//            }
//            if (monthlySummary != null && monthlySummary.getTotalSalesCount() != null) { // DTO 필드명 확인
//                totalOrdersForPeriod += monthlySummary.getTotalSalesCount();
//            }
//        }
//        stats.put("totalSalesInPeriod", totalSalesForPeriod);
//        stats.put("totalOrdersInPeriod", totalOrdersForPeriod);
//        return stats;
//    }
//
//    @Override
//    @Transactional
//    public SellerDTO approveSeller(Integer sellerId, boolean approved, String message) {
//        Seller seller = sellerRepository.findById(sellerId.longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + sellerId));
//        seller.setApproved(approved);
//        if (approved) seller.setApprovedAt(LocalDateTime.now());
//        log.info("판매자 ID {} 처리 (관리). 승인: {}, 메시지: {}", sellerId, approved, message);
//        return convertToSellerDTO(sellerRepository.save(seller));
//    }
//
//    private SellerDTO convertToSellerDTO(Seller e) {
//        return SellerDTO.builder().id(e.getId()).name(e.getName()).email(e.getEmail()).status(e.isApproved() ? "APPROVED" : "PENDING").registeredAt(e.getCreatedAt()).build();
//    }
//    private ProductDTO convertToProductDTO(Product e) {
//        return ProductDTO.builder().id(e.getId()).name(e.getName()).price(BigDecimal.valueOf(e.getPrice())).inventory(e.getStock()).status(e.getStatus() != null ? e.getStatus().name() : null).sellerId(e.getSeller() != null ? e.getSeller().getId() : null).sellerName(e.getSeller() != null ? e.getSeller().getName() : null).registeredAt(e.getCreatedAt()).build();
//    }
//}
