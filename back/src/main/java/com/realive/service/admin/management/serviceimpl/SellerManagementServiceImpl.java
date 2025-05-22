package com.realive.service.admin.management.serviceimpl;


import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;
import com.realive.repository.ProductRepository;
import com.realive.repository.SellerRepository;
import com.realive.service.admin.management.sellerman.SellerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerManagementServiceImpl implements SellerManagementService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<SellerDTO> getSellers(Pageable pageable) {
        return sellerRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<SellerDTO> searchSellers(String keyword, Pageable pageable) {
        return sellerRepository.findByNameContaining(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public SellerDTO getSellerById(Integer sellerId) {
        return sellerRepository.findById(sellerId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("판매자 ID가 존재하지 않습니다: " + sellerId));
    }

    @Override
    @Transactional
    public SellerDTO updateSellerStatus(Integer sellerId, String status) {
        var seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("판매자 ID가 존재하지 않습니다: " + sellerId));

        seller.setStatus(status);
        return convertToDTO(sellerRepository.save(seller));
    }

    @Override
    @Transactional
    public SellerDTO updateSellerCommission(Integer sellerId, BigDecimal commission) {
        var seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("판매자 ID가 존재하지 않습니다: " + sellerId));

        seller.setCommission(commission);
        return convertToDTO(sellerRepository.save(seller));
    }

    @Override
    public Page<ProductDTO> getSellerProducts(Integer sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable)
                .map(this::convertToProductDTO);
    }

    @Override
    public Map<String, Object> getSellerSalesStatistics(Integer sellerId, LocalDate startDate, LocalDate endDate) {
        // 판매 총액
        BigDecimal totalSales = sellerRepository.getTotalSalesBySellerId(
                sellerId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // 판매 건수
        Long orderCount = sellerRepository.getOrderCountBySellerId(
                sellerId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // 판매 상품 수
        Long productCount = productRepository.countBySellerId(sellerId);

        // 판매 수수료
        BigDecimal commission = sellerRepository.findById(sellerId)
                .map(Seller::getCommission)
                .orElse(BigDecimal.ZERO);

        // 일별 판매 추이
        Map<LocalDate, BigDecimal> dailySales = sellerRepository.getDailySalesBySellerId(
                sellerId, startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        statistics.put("orderCount", orderCount);
        statistics.put("productCount", productCount);
        statistics.put("commission", commission);
        statistics.put("estimatedCommissionAmount",
                totalSales != null ? totalSales.multiply(commission.divide(BigDecimal.valueOf(100))) : BigDecimal.ZERO);
        statistics.put("dailySales", dailySales);

        return statistics;
    }

    @Override
    @Transactional
    public SellerDTO approveSeller(Integer sellerId, boolean approved, String message) {
        var seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("판매자 ID가 존재하지 않습니다: " + sellerId));

        if (approved) {
            seller.setStatus("APPROVED");
        } else {
            seller.setStatus("REJECTED");
            seller.setRejectionReason(message);
        }

        return convertToDTO(sellerRepository.save(seller));
    }

    // 엔티티 -> DTO 변환 메소드
    private SellerDTO convertToDTO(Seller seller) {
        SellerDTO dto = new SellerDTO();
        dto.setId(seller.getId());
        dto.setName(seller.getName());
        dto.setStatus(seller.getStatus());
        dto.setRegisteredAt(seller.getRegisteredAt());
        dto.setProductCount(seller.getProductCount());
        dto.setTotalSales(seller.getTotalSales());
        dto.setCommission(seller.getCommission());
        return dto;
    }

    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getName());
        dto.setStatus(product.getStatus());
        dto.setPrice(product.getPrice());
        dto.setInventory(product.getInventory());
        dto.setRegisteredAt(product.getRegisteredAt());
        return dto;
    }
}