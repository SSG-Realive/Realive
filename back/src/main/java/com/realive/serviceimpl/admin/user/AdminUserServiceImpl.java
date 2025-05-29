package com.realive.serviceimpl.admin.user;


import com.realive.domain.customer.Customer;
import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerReview; // SellerReview 엔티티 (판매자 삭제 시 리뷰 조회용)
import com.realive.dto.admin.user.CustomerDetailDTO; // 추가
import com.realive.dto.admin.user.SellerDetailDTO;   // 추가
import com.realive.dto.admin.user.UserManagementListItemDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
// --- 기타 필요한 Repository (Product, SalesLog 등) 주입 ---
// import com.realive.repository.product.ProductRepository;
// import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.seller.SellerRepository;
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
    // --- 기타 Repository 필드 선언 및 생성자 주입 ---
    // private final ProductRepository productRepository;
    // private final SalesLogRepository salesLogRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementListItemDTO> getAllUsers(
            Pageable pageable,
            Optional<String> userTypeFilter,
            Optional<String> searchTerm,
            Optional<Boolean> activeFilter) {
        // (이전 최종본의 getAllUsers 메소드 내용과 동일 - 생략)
        String type = userTypeFilter.orElse("").toUpperCase();
        String keyword = searchTerm.orElse("").toLowerCase().trim();

        List<UserManagementListItemDTO> finalContentList = new ArrayList<>();
        long totalMatchingElements = 0;

        if ("CUSTOMER".equals(type)) {
            Page<Customer> customerPage = customerRepository.findAll(pageable);
            List<UserManagementListItemDTO> filteredCustomers = customerPage.getContent().stream()
                    .filter(customer -> activeFilter.map(active -> customer.getIsActive() == active).orElse(true))
                    .filter(customer -> keyword.isEmpty() ||
                            (customer.getName() != null && customer.getName().toLowerCase().contains(keyword)) ||
                            (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            finalContentList.addAll(filteredCustomers);
            totalMatchingElements = customerRepository.count();

        } else if ("SELLER".equals(type)) {
            Page<Seller> sellerPage = sellerRepository.findAll(pageable);
            List<UserManagementListItemDTO> filteredSellers = sellerPage.getContent().stream()
                    .filter(seller -> activeFilter.map(active -> seller.isActive() == active).orElse(true))
                    .filter(seller -> keyword.isEmpty() ||
                            (seller.getName() != null && seller.getName().toLowerCase().contains(keyword)) ||
                            (seller.getEmail() != null && seller.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            finalContentList.addAll(filteredSellers);
            totalMatchingElements = sellerRepository.count();

        } else {
            log.warn("Fetching ALL user types with in-memory filtering. This can be inefficient for large datasets.");
            List<Customer> allCustomers = customerRepository.findAll(pageable.getSort());
            List<Seller> allSellers = sellerRepository.findAll(pageable.getSort());

            List<UserManagementListItemDTO> allUserDTOs = new ArrayList<>();
            allCustomers.stream()
                    .filter(customer -> activeFilter.map(active -> customer.getIsActive() == active).orElse(true))
                    .filter(customer -> keyword.isEmpty() ||
                            (customer.getName() != null && customer.getName().toLowerCase().contains(keyword)) ||
                            (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .forEach(allUserDTOs::add);
            allSellers.stream()
                    .filter(seller -> activeFilter.map(active -> seller.isActive() == active).orElse(true))
                    .filter(seller -> keyword.isEmpty() ||
                            (seller.getName() != null && seller.getName().toLowerCase().contains(keyword)) ||
                            (seller.getEmail() != null && seller.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .forEach(allUserDTOs::add);

            allUserDTOs.sort(Comparator.comparing(UserManagementListItemDTO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

            totalMatchingElements = allUserDTOs.size();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allUserDTOs.size());
            finalContentList = (start > totalMatchingElements) ? List.of() : allUserDTOs.subList(start, end);
        }
        return new PageImpl<>(finalContentList, pageable, totalMatchingElements);
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, String userType, boolean newIsActive)
            throws EntityNotFoundException, IllegalArgumentException {
        // (이전 최종본의 updateUserStatus 메소드 내용과 동일 - 생략)
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

    @Override
    @Transactional
    public void deleteUser(Long userId, String userType)
            throws EntityNotFoundException, IllegalArgumentException, DataIntegrityViolationException {
        // (이전 최종본의 deleteUser 메소드 내용과 동일 - 연관 데이터 삭제 로직 포함)
        // 상세 로직은 이전 답변 참고 (Order, SellerReview, ReviewReport 등 삭제)
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId + " for deletion."));
            try {
                log.info("Deleting related data for Customer (ID: {})...", userId);

                orderRepository.deleteByCustomerId(userId);
                sellerReviewRepository.deleteByCustomerId(userId);
                reviewReportRepository.deleteByReporterId(userId.intValue()); // 타입 변환 주의
                // ... 기타 고객 관련 연관 데이터 삭제 로직 (예: 입찰 내역 등) ...

                customerRepository.delete(customer);
                log.info("Customer (ID: {}) physically deleted successfully.", userId);
            } catch (DataIntegrityViolationException e) { /* ... */ } catch (Exception e) { /* ... */ }

        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId + " for deletion."));
            try {
                log.info("Deleting related data for Seller (ID: {})...", userId);

                Page<SellerReview> sellerReceivedReviewsPage = sellerReviewRepository.findReviewsBySellerId(userId, Pageable.unpaged());
                List<SellerReview> sellerReceivedReviews = sellerReceivedReviewsPage.getContent();
                if (!sellerReceivedReviews.isEmpty()) {
                    List<Integer> sellerReceivedReviewIds = sellerReceivedReviews.stream()
                            .map(sr -> sr.getId().intValue())
                            .collect(Collectors.toList());
                    if (!sellerReceivedReviewIds.isEmpty()) {
                        reviewReportRepository.deleteAllBySellerReviewIdIn(sellerReceivedReviewIds);
                    }
                }
                sellerReviewRepository.deleteBySellerId(userId);
                // productRepository.deleteBySellerId(userId); // 예시
                // salesLogRepository.deleteBySellerId(userId); // 예시
                // ... 기타 판매자 관련 연관 데이터 삭제 로직 ...

                sellerRepository.delete(seller);
                log.info("Seller (ID: {}) physically deleted successfully.", userId);
            } catch (DataIntegrityViolationException e) { /* ... */ } catch (Exception e) { /* ... */ }
        } else { /* ... */ }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailDTO getCustomerDetails(Long customerId)
            throws EntityNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        return convertToCustomerDetailDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDetailDTO getSellerDetails(Long sellerId)
            throws EntityNotFoundException {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
        return convertToSellerDetailDTO(seller);
    }

    // --- DTO 변환 메소드들 ---
    private UserManagementListItemDTO convertToUserManagementDTO(Customer customer) { /* 이전과 동일 */
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
    private UserManagementListItemDTO convertToUserManagementDTO(Seller seller) { /* 이전과 동일 */
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
