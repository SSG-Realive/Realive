package com.realive.serviceimpl.admin.user;


import com.realive.domain.customer.Customer;
import com.realive.domain.seller.Seller;
import com.realive.domain.order.Order;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.domain.review.ReviewReport;
import com.realive.domain.seller.SellerReview;
// import com.realive.domain.product.Product; // 판매자 상품 처리 로직 추가 시 필요

import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
// import com.realive.repository.product.ProductRepository; // 판매자 상품 처리 로직 추가 시 필요

import com.realive.dto.admin.user.CustomerDetailDTO;
import com.realive.dto.admin.user.SellerDetailDTO;
import com.realive.dto.admin.user.UserManagementListItemDTO;
import com.realive.service.admin.user.AdminUserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    // private final ProductRepository productRepository; // 판매자 상품 처리 로직 추가 시 필요

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementListItemDTO> getAllUsers(
            Pageable pageable,
            Optional<String> userTypeFilter,
            Optional<String> searchTerm,
            Optional<Boolean> activeFilter) {
        // 사용자님이 이전에 보여주신 코드의 로직을 유지합니다.
        // (이 부분은 Repository 레벨에서 필터링/페이징하는 것이 성능상 더 효율적일 수 있습니다.)
        String type = userTypeFilter.orElse("").toUpperCase();
        String keyword = searchTerm.orElse("").toLowerCase().trim();
        List<UserManagementListItemDTO> finalContentList = new ArrayList<>();
        long totalMatchingElements = 0;

        if ("CUSTOMER".equals(type) || type.isEmpty()) {
            List<Customer> customers = customerRepository.findAll(); // 실제로는 필터링된 결과를 가져오는 것이 좋음
            List<UserManagementListItemDTO> customerDTOs = customers.stream()
                    // === 수정된 라인 (람다식 사용) ===
                    .filter(customer -> activeFilter.map(active -> customer.getIsActive() == active).orElse(true))
                    .filter(customer -> keyword.isEmpty() ||
                            (customer.getName() != null && customer.getName().toLowerCase().contains(keyword)) ||
                            (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            if ("CUSTOMER".equals(type)) {
                finalContentList.addAll(customerDTOs);
                totalMatchingElements = customerDTOs.size();
            } else if (type.isEmpty()) {
                finalContentList.addAll(customerDTOs);
            }
        }

        if ("SELLER".equals(type) || type.isEmpty()) {
            List<Seller> sellers = sellerRepository.findAll(); // 실제로는 필터링된 결과를 가져오는 것이 좋음
            List<UserManagementListItemDTO> sellerDTOs = sellers.stream()
                    // === 수정된 라인 (람다식 사용) ===
                    .filter(seller -> activeFilter.map(active -> seller.isActive() == active).orElse(true))
                    .filter(seller -> keyword.isEmpty() ||
                            (seller.getName() != null && seller.getName().toLowerCase().contains(keyword)) ||
                            (seller.getEmail() != null && seller.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            if ("SELLER".equals(type)) {
                finalContentList.addAll(sellerDTOs);
                totalMatchingElements = sellerDTOs.size();
            } else if (type.isEmpty()) {
                finalContentList.addAll(sellerDTOs);
            }
        }

        if (type.isEmpty()) { // CUSTOMER 와 SELLER 모두 조회 시, 정렬 및 페이징
            finalContentList.sort(Comparator.comparing(UserManagementListItemDTO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
            totalMatchingElements = finalContentList.size();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), finalContentList.size());
            finalContentList = (start >= totalMatchingElements) ? List.of() : finalContentList.subList(start, end);
        }
        return new PageImpl<>(finalContentList, pageable, totalMatchingElements);
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, String userType, boolean newIsActive)
            throws EntityNotFoundException, IllegalArgumentException {
        // 사용자님이 첨부해주신 [파일 1]의 로직을 유지합니다.
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId));
            customer.setIsActive(newIsActive);
            customerRepository.save(customer);
            log.info("Customer (ID: {}) status updated to: {}", userId, newIsActive);
            return true;
        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId));
            seller.setActive(newIsActive);
            sellerRepository.save(seller);
            log.info("Seller (ID: {}) status updated to: {}", userId, newIsActive);
            return true;
        } else {
            log.warn("Invalid userType provided for status update: {}", userType);
            throw new IllegalArgumentException("Invalid userType: " + userType + ". Must be CUSTOMER or SELLER.");
        }
    }

    /**
     * 사용자(고객 또는 판매자) 계정을 비활성화하고 관련 데이터를 처리합니다.
     * 인터페이스의 deleteUser 메소드 시그니처를 유지하며, 내부적으로 비활성화 로직을 수행합니다.
     */
    @Override
    @Transactional
    public void deleteUser(Long userId, String userType)
            throws EntityNotFoundException, IllegalArgumentException, DataIntegrityViolationException {
        log.info("Processing user deactivation for {} (ID: {})", userType, userId);

        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId + " for deactivation."));

            // 1. Customer 엔티티 비활성화 (isActive = false)
            customer.setIsActive(false);
            customerRepository.save(customer);
            log.info("Customer (ID: {}) has been set to inactive.", customer.getId());

            // 2. 해당 고객의 주문(Order)들의 상태를 'ORDER_CLOSED_BY_USER_DELETION'으로 변경
            List<Order> customerOrders = orderRepository.findAllByCustomerId(customer.getId());
            if (customerOrders != null && !customerOrders.isEmpty()) {
                log.info("Updating status to ORDER_CLOSED_BY_USER_DELETION for {} orders of customer ID: {}",
                        customerOrders.size(), customer.getId());
                for (Order order : customerOrders) {
                    order.setStatus(OrderStatus.ORDER_CLOSED_BY_USER_DELETION);
                }
                orderRepository.saveAll(customerOrders);
            }

            // 3. 해당 고객이 작성한 판매자 리뷰(SellerReview) 숨김 처리 (isHidden = true)
            List<SellerReview> customerReviews = sellerReviewRepository.findAllByCustomerId(customer.getId());
            if (customerReviews != null && !customerReviews.isEmpty()) {
                log.info("Hiding {} seller reviews written by customer ID: {}", customerReviews.size(), customer.getId());
                for (SellerReview review : customerReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(customerReviews);
            }

            // 4. 해당 고객이 신고한 리뷰 신고(ReviewReport) 상태 변경
            if (customer.getId() != null) {
                Integer reporterIdAsInt = null;
                try {
                    reporterIdAsInt = Math.toIntExact(customer.getId());
                } catch (ArithmeticException e) {
                    log.warn("Customer ID {} is too large to be converted to Integer for reporterId lookup. Skipping ReviewReport update for this customer.", customer.getId(), e);
                }

                if (reporterIdAsInt != null) {
                    List<ReviewReport> customerReports = reviewReportRepository.findAllByReporterId(reporterIdAsInt);
                    if (customerReports != null && !customerReports.isEmpty()) {
                        log.info("Updating status to REPORTER_ACCOUNT_INACTIVE for {} review reports by customer ID: {} (reporterId: {})",
                                customerReports.size(), customer.getId(), reporterIdAsInt);
                        for (ReviewReport report : customerReports) {
                            report.updateStatus(ReviewReportStatus.REPORTER_ACCOUNT_INACTIVE); // ReviewReport 엔티티의 updateStatus() 사용
                        }
                        reviewReportRepository.saveAll(customerReports);
                    }
                }
            }

        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId + " for deactivation."));

            // 1. Seller 엔티티 비활성화 (isActive = false)
            seller.setActive(false);
            sellerRepository.save(seller);
            log.info("Seller (ID: {}) has been set to inactive.", seller.getId());

            // 2. 해당 판매자가 받은 판매자 리뷰(SellerReview) 숨김 처리 (isHidden = true)
            List<SellerReview> sellerReceivedReviews = sellerReviewRepository.findAllBySellerId(seller.getId());
            if (sellerReceivedReviews != null && !sellerReceivedReviews.isEmpty()) {
                log.info("Hiding {} seller reviews received by seller ID: {}", sellerReceivedReviews.size(), seller.getId());
                for (SellerReview review : sellerReceivedReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(sellerReceivedReviews);
            }

            // 3. 해당 판매자의 상품(Product) 판매 중지 처리 (선택적 로직)
            log.info("Products for seller ID: {} should be handled (e.g., set to inactive). This part is optional and needs ProductRepository.", seller.getId());
            /*
            // 예시 (ProductRepository 주입 및 Product 엔티티에 setActive(boolean) 필요):
            // private final ProductRepository productRepository;
            List<Product> sellerProducts = productRepository.findBySellerId(seller.getId());
            if (sellerProducts != null && !sellerProducts.isEmpty()) {
                log.info("Deactivating {} products for seller ID: {}", sellerProducts.size(), seller.getId());
                for (Product product : sellerProducts) {
                    product.setActive(false);
                }
                productRepository.saveAll(sellerProducts);
            }
            */

        } else {
            log.warn("Invalid userType provided for deactivation: {}", userType);
            throw new IllegalArgumentException("Invalid userType: " + userType + ". Must be CUSTOMER or SELLER.");
        }
        log.info("Successfully processed user deactivation for {} (ID: {})", userType, userId);
    }


    @Override
    @Transactional(readOnly = true)
    public CustomerDetailDTO getCustomerDetails(Long customerId)
            throws EntityNotFoundException {
        // 사용자님이 첨부해주신 [파일 1]의 로직을 유지합니다.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        return convertToCustomerDetailDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDetailDTO getSellerDetails(Long sellerId)
            throws EntityNotFoundException {
        // 사용자님이 첨부해주신 [파일 1]의 로직을 유지합니다.
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
        return convertToSellerDetailDTO(seller);
    }

    // --- DTO 변환 메소드들 (사용자님이 첨부해주신 [파일 1]의 로직을 유지합니다.) ---
    private UserManagementListItemDTO convertToUserManagementDTO(Customer customer) {
        return UserManagementListItemDTO.builder()
                .id(customer.getId())
                .userType("CUSTOMER")
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreated())
                .isApproved(null)
                .build();
    }

    private UserManagementListItemDTO convertToUserManagementDTO(Seller seller) {
        return UserManagementListItemDTO.builder()
                .id(seller.getId())
                .userType("SELLER")
                .name(seller.getName())
                .email(seller.getEmail())
                .phone(seller.getPhone())
                .isActive(seller.isActive())
                .createdAt(seller.getCreatedAt())
                .isApproved(seller.isApproved())
                .build();
    }

    private CustomerDetailDTO convertToCustomerDetailDTO(Customer customer) {
        return CustomerDetailDTO.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .name(customer.getName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .isVerified(customer.getIsVerified())
                .isActive(customer.getIsActive())
                .penaltyScore(customer.getPenaltyScore())
                .birth(customer.getBirth())
                .gender(customer.getGender())
                .signupMethod(customer.getSignupMethod())
                .createdAt(customer.getCreated())
                .updatedAt(customer.getUpdated())
                .build();
    }

    private SellerDetailDTO convertToSellerDetailDTO(Seller seller) {
        return SellerDetailDTO.builder()
                .id(seller.getId())
                .name(seller.getName())
                .email(seller.getEmail())
                .phone(seller.getPhone())
                .businessNumber(seller.getBusinessNumber())
                .isApproved(seller.isApproved())
                .approvedAt(seller.getApprovedAt())
                .isActive(seller.isActive())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
    }
}
