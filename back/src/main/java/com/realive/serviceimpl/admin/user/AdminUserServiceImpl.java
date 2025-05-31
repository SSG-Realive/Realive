package com.realive.serviceimpl.admin.user;

// --- 필요한 import 문들 (이전 답변과 유사) ---
import com.realive.domain.customer.Customer;
import com.realive.domain.seller.Seller;
import com.realive.domain.order.Order;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.ReviewReportStatus; // ReviewReportStatus Enum
import com.realive.domain.review.ReviewReport; // ReviewReport 엔티티
import com.realive.domain.seller.SellerReview;

import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
// import com.realive.repository.product.ProductRepository; // 필요시

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
    // private final ProductRepository productRepository; // 필요시 주입

    // --- getAllUsers, updateUserStatus, getCustomerDetails, getSellerDetails ---
    // --- 및 DTO 변환 메소드들은 이전 최종본 답변의 코드와 동일하게 유지합니다. ---
    // --- (코드 길이상 생략) ---
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementListItemDTO> getAllUsers(
            Pageable pageable,
            Optional<String> userTypeFilter,
            Optional<String> searchTerm,
            Optional<Boolean> activeFilter) {
        // 이전 최종본 답변의 코드 내용 (사용자님이 첨부해주신 [파일 1] 기반)
        String type = userTypeFilter.orElse("").toUpperCase();
        String keyword = searchTerm.orElse("").toLowerCase().trim();
        List<UserManagementListItemDTO> finalContentList = new ArrayList<>();
        long totalMatchingElements = 0;

        if ("CUSTOMER".equals(type) || type.isEmpty()) {
            List<Customer> customers = customerRepository.findAll();
            List<UserManagementListItemDTO> customerDTOs = customers.stream()
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
            List<Seller> sellers = sellerRepository.findAll();
            List<UserManagementListItemDTO> sellerDTOs = sellers.stream()
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
        if (type.isEmpty()) {
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
        // 이전 최종본 답변의 코드 내용
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
     * (deleteUser 메소드의 기능을 "비활성화"로 변경)
     */
    @Override
    @Transactional
    public void deleteUser(Long userId, String userType)
            throws EntityNotFoundException, IllegalArgumentException, DataIntegrityViolationException {
        log.info("Processing user deactivation for {} (ID: {})", userType, userId);

        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId + " for deactivation."));

            // 1. Customer 엔티티 비활성화
            customer.setIsActive(false);
            customerRepository.save(customer);
            log.info("Customer (ID: {}) has been set to inactive.", customer.getId());

            // 2. 해당 고객의 주문(Order) 상태 변경
            List<Order> customerOrders = orderRepository.findAllByCustomerId(customer.getId());
            if (customerOrders != null && !customerOrders.isEmpty()) {
                log.info("Updating status to ORDER_CLOSED_BY_USER_DELETION for {} orders of customer ID: {}",
                        customerOrders.size(), customer.getId());
                for (Order order : customerOrders) {
                    order.setStatus(OrderStatus.ORDER_CLOSED_BY_USER_DELETION);
                }
                orderRepository.saveAll(customerOrders);
            }

            // 3. 해당 고객이 작성한 판매자 리뷰(SellerReview) 숨김 처리
            List<SellerReview> customerReviews = sellerReviewRepository.findAllByCustomerId(customer.getId());
            if (customerReviews != null && !customerReviews.isEmpty()) {
                log.info("Hiding {} seller reviews written by customer ID: {}", customerReviews.size(), customer.getId());
                for (SellerReview review : customerReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(customerReviews);
            }

            // 4. 해당 고객이 신고한 리뷰 신고(ReviewReport) 상태 변경
            // ReviewReport 엔티티의 reporterId 타입과 Customer ID 타입 일치 또는 변환 필요
            // ReviewReport.reporterId가 Integer라고 가정
            if (customer.getId() != null) {
                Integer reporterIdAsInt = null;
                try {
                    // Long to Integer 변환 시 데이터 손실 가능성 주의
                    reporterIdAsInt = customer.getId().intValue();
                } catch (ArithmeticException e) {
                    log.warn("Customer ID {} is too large to be converted to Integer for reporterId lookup.", customer.getId(), e);
                    // 적절한 예외 처리 또는 로깅. 여기서는 null로 두고 아래에서 null 체크.
                }

                if (reporterIdAsInt != null) {
                    List<ReviewReport> customerReports = reviewReportRepository.findAllByReporterId(reporterIdAsInt);
                    if (customerReports != null && !customerReports.isEmpty()) {
                        log.info("Updating status to REPORTER_ACCOUNT_INACTIVE for {} review reports by customer ID: {} (reporterId: {})",
                                customerReports.size(), customer.getId(), reporterIdAsInt);
                        for (ReviewReport report : customerReports) {
                            report.updateStatus(ReviewReportStatus.REPORTER_ACCOUNT_INACTIVE); // <<< updateStatus 사용
                        }
                        reviewReportRepository.saveAll(customerReports);
                    }
                }
            }

        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId + " for deactivation."));

            // 1. Seller 엔티티 비활성화
            seller.setActive(false);
            sellerRepository.save(seller);
            log.info("Seller (ID: {}) has been set to inactive.", seller.getId());

            // 2. 해당 판매자가 받은 판매자 리뷰(SellerReview) 숨김 처리
            List<SellerReview> sellerReceivedReviews = sellerReviewRepository.findAllBySellerId(seller.getId());
            if (sellerReceivedReviews != null && !sellerReceivedReviews.isEmpty()) {
                log.info("Hiding {} seller reviews received by seller ID: {}", sellerReceivedReviews.size(), seller.getId());
                for (SellerReview review : sellerReceivedReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(sellerReceivedReviews);
            }

            // 3. 해당 판매자의 상품(Product) 판매 중지 처리 (선택적)
            log.info("Products for seller ID: {} should be handled (e.g., set to inactive).", seller.getId());
            // 예: productRepository.deactivateProductsBySellerId(seller.getId());

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
        // 이전 최종본 답변의 코드 내용 (사용자님이 첨부해주신 [파일 1] 기반)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        return convertToCustomerDetailDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDetailDTO getSellerDetails(Long sellerId)
            throws EntityNotFoundException {
        // 이전 최종본 답변의 코드 내용 (사용자님이 첨부해주신 [파일 1] 기반)
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
        return convertToSellerDetailDTO(seller);
    }

    // --- DTO 변환 메소드들 (이전 최종본 답변의 코드와 동일) ---
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
