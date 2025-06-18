package com.realive.serviceimpl.admin.user; // 패키지 경로는 실제 프로젝트에 맞게 확인해주세요.

import com.realive.domain.customer.Customer;
import com.realive.domain.seller.Seller;
import com.realive.domain.order.Order;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.ReviewReportStatus;
import com.realive.domain.review.ReviewReport;
import com.realive.domain.seller.SellerReview;
// import com.realive.domain.product.Product; // 판매자 상품 처리 로직 추가 시 필요 (현재 사용 안 함)

import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.ReviewReportRepository;
import com.realive.repository.review.SellerReviewRepository;
// import com.realive.repository.product.ProductRepository; // 판매자 상품 처리 로직 추가 시 필요 (현재 사용 안 함)

import com.realive.dto.admin.user.CustomerDetailDTO;
import com.realive.dto.admin.user.SellerDetailDTO;
import com.realive.dto.admin.user.UserManagementListItemDTO;
import com.realive.service.admin.user.AdminUserService; // AdminUserService 인터페이스 임포트

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService { // AdminUserService 인터페이스 구현

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    // private final ProductRepository productRepository; // 판매자 상품 처리 로직 추가 시 필요 (현재 사용 안 함)

    /**
     * 전체 사용자 목록을 조회합니다. 고객과 판매자를 모두 포함하며, 필터링, 페이징, 정렬 기능을 지원합니다.
     *
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
     * @param userTypeFilter 사용자 유형 필터 (CUSTOMER, SELLER, 또는 null/empty for all)
     * @param searchTerm 검색어 (이름 또는 이메일 주소로 검색)
     * @param activeFilter 활성 상태 필터 (true: 활성 사용자만, false: 비활성 사용자만, null: 전체)
     * @return 페이징된 사용자 목록 (UserManagementListItemDTO)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementListItemDTO> getAllUsers(
            Pageable pageable,
            Optional<String> userTypeFilter,
            Optional<String> searchTerm,
            Optional<Boolean> activeFilter) {

        log.info("Fetching all users with pageable: {}, userTypeFilter: {}, searchTerm: {}, activeFilter: {}",
                pageable, userTypeFilter, searchTerm, activeFilter);

        String type = userTypeFilter.orElse("").toUpperCase();
        String keyword = searchTerm.orElse("").toLowerCase().trim();

        List<UserManagementListItemDTO> filteredAndCombinedDTOs = new ArrayList<>();

        // 1. 고객(Customer) 데이터 필터링 및 DTO 변환
        if (type.isEmpty() || "CUSTOMER".equals(type)) {
            List<Customer> customers = customerRepository.findAll();
            List<UserManagementListItemDTO> customerDTOs = customers.stream()
                    .filter(customer -> activeFilter.map(active -> customer.getIsActive() == active).orElse(true))
                    .filter(customer -> keyword.isEmpty() ||
                            (customer.getName() != null && customer.getName().toLowerCase().contains(keyword)) ||
                            (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            filteredAndCombinedDTOs.addAll(customerDTOs);
        }

        // 2. 판매자(Seller) 데이터 필터링 및 DTO 변환
        if (type.isEmpty() || "SELLER".equals(type)) {
            List<Seller> sellers = sellerRepository.findAll();
            List<UserManagementListItemDTO> sellerDTOs = sellers.stream()
                    .filter(seller -> activeFilter.map(active -> seller.isActive() == active).orElse(true))
                    .filter(seller -> keyword.isEmpty() ||
                            (seller.getName() != null && seller.getName().toLowerCase().contains(keyword)) ||
                            (seller.getEmail() != null && seller.getEmail().toLowerCase().contains(keyword)))
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());
            filteredAndCombinedDTOs.addAll(sellerDTOs);
        }

        // 3. 전체 요소 개수 (페이징 전)
        long totalElements = filteredAndCombinedDTOs.size();

        // 4. Pageable의 Sort 정보에 따라 정렬
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Comparator<UserManagementListItemDTO> comparator = null;
            for (Sort.Order order : sort) {
                Comparator<UserManagementListItemDTO> currentComparator = null;
                String property = order.getProperty();

                switch (property) {
                    case "id":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getId, Comparator.nullsLast(Long::compareTo));
                        break;
                    case "name":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        break;
                    case "email":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        break;
                    case "createdAt":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
                        break;
                    case "userType":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getUserType, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        break;
                    case "isActive":
                        currentComparator = Comparator.comparing(UserManagementListItemDTO::getIsActive, Comparator.nullsLast(Boolean::compareTo));
                        break;
                    default:
                        log.warn("Unsupported sort property: {} in AdminUserServiceImpl.getAllUsers", property);
                        continue;
                }

                if (order.isDescending()) {
                    currentComparator = currentComparator.reversed();
                }

                if (comparator == null) {
                    comparator = currentComparator;
                } else {
                    comparator = comparator.thenComparing(currentComparator);
                }
            }

            if (comparator != null) {
                filteredAndCombinedDTOs.sort(comparator);
            }
        }

        // 5. 정렬된 리스트에 대해 페이징 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), (int) totalElements);

        List<UserManagementListItemDTO> pageContent = Collections.emptyList();
        if (start <= end && start < totalElements) {
            pageContent = filteredAndCombinedDTOs.subList(start, end);
        }

        log.info("Returning {} users for page {} (size {}) with total elements {}", pageContent.size(), pageable.getPageNumber(), pageable.getPageSize(), totalElements);
        // === Error 2 수정: PageImpl 생성자 수정 ===
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    /**
     * 사용자의 활성 상태를 변경합니다.
     *
     * @param userId 변경할 사용자 ID
     * @param userType 사용자 유형 (CUSTOMER 또는 SELLER)
     * @param newIsActive 새로운 활성 상태 값
     * @return 작업 성공 여부 (항상 true를 반환하거나, 예외 발생 시 호출자에게 전달)
     * @throws EntityNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     * @throws IllegalArgumentException 잘못된 사용자 유형이 제공된 경우
     */
    @Override // AdminUserService 인터페이스의 메소드를 오버라이드
    @Transactional
    public boolean updateUserStatus(Long userId, String userType, boolean newIsActive)
            throws EntityNotFoundException, IllegalArgumentException {
        // === Error 1 관련: 이 메소드 시그니처가 AdminUserService 인터페이스와 정확히 일치하는지 확인해주세요. ===
        // throws 절의 예외들은 RuntimeException이므로 인터페이스에 명시되지 않아도 됩니다.
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId));
            customer.setIsActive(newIsActive);
            // customer.setUpdated(LocalDateTime.now()); // Customer 엔티티에 setUpdated(LocalDateTime) 메소드가 있는지 확인 필요
            customerRepository.save(customer);
            log.info("Customer (ID: {}) status updated to: {}", userId, newIsActive);
            return true;
        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId));
            seller.setActive(newIsActive);
            // === Error 3 관련: Seller 엔티티에 setUpdatedAt(LocalDateTime) 메소드가 있는지 확인해주세요. 없다면 이 라인은 주석 처리하거나 Seller 엔티티를 수정해야 합니다. ===
            // seller.setUpdatedAt(LocalDateTime.now());
            sellerRepository.save(seller);
            log.info("Seller (ID: {}) status updated to: {}", userId, newIsActive);
            return true;
        } else {
            log.warn("Invalid userType provided for status update: {}", userType);
            throw new IllegalArgumentException("Invalid userType: " + userType + ". Must be CUSTOMER or SELLER.");
        }
    }

    /**
     * 특정 ID의 사용자를 비활성화합니다. (실제 데이터 삭제는 하지 않고, is_active 플래그를 false로 설정)
     * 또한 관련된 주문, 리뷰, 신고 데이터의 상태를 업데이트합니다.
     *
     * @param userId 비활성화할 사용자 ID
     * @param userType 사용자 유형 (CUSTOMER 또는 SELLER)
     * @throws EntityNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     * @throws IllegalArgumentException 잘못된 사용자 유형이 제공된 경우
     * @throws DataIntegrityViolationException 데이터 무결성 제약 조건 위반 시 (현재는 직접 발생시키지 않음)
     */
    @Override // AdminUserService 인터페이스의 메소드를 오버라이드
    @Transactional
    public void deleteUser(Long userId, String userType)
            throws EntityNotFoundException, IllegalArgumentException, DataIntegrityViolationException {
        log.info("Processing user deactivation for {} (ID: {})", userType, userId);

        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + userId + " for deactivation."));

            customer.setIsActive(false);
            // customer.setUpdated(LocalDateTime.now()); // Customer 엔티티에 setUpdated(LocalDateTime) 메소드가 있는지 확인 필요
            customerRepository.save(customer);
            log.info("Customer (ID: {}) has been set to inactive.", customer.getId());

            List<Order> customerOrders = orderRepository.findAllByCustomerId(customer.getId());
            if (customerOrders != null && !customerOrders.isEmpty()) {
                log.info("Updating status to ORDER_CLOSED_BY_USER_DELETION for {} orders of customer ID: {}",
                        customerOrders.size(), customer.getId());
                for (Order order : customerOrders) {
                    order.setStatus(OrderStatus.ORDER_CLOSED_BY_USER_DELETION);
                }
                orderRepository.saveAll(customerOrders);
            }

            List<SellerReview> customerReviews = sellerReviewRepository.findAllByCustomerId(customer.getId());
            if (customerReviews != null && !customerReviews.isEmpty()) {
                log.info("Hiding {} seller reviews written by customer ID: {}", customerReviews.size(), customer.getId());
                for (SellerReview review : customerReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(customerReviews);
            }

            Integer reporterIdAsInt = null;
            try {
                reporterIdAsInt = Math.toIntExact(customer.getId());
            } catch (ArithmeticException e) {
                log.warn("Customer ID {} is too large to be converted to Integer for reporterId lookup. Skipping ReviewReport update.", customer.getId(), e);
            }

            if (reporterIdAsInt != null) {
                List<ReviewReport> customerReports = reviewReportRepository.findAllByReporterId(reporterIdAsInt);
                if (customerReports != null && !customerReports.isEmpty()) {
                    log.info("Updating status to REPORTER_ACCOUNT_INACTIVE for {} review reports by customer ID: {} (reporterId: {})",
                            customerReports.size(), customer.getId(), reporterIdAsInt);
                    for (ReviewReport report : customerReports) {
                        report.updateStatus(ReviewReportStatus.REPORTER_ACCOUNT_INACTIVE);
                    }
                    reviewReportRepository.saveAll(customerReports);
                }
            }

        } else if ("SELLER".equalsIgnoreCase(userType)) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + userId + " for deactivation."));

            seller.setActive(false);
            // === Error 3 관련: Seller 엔티티에 setUpdatedAt(LocalDateTime) 메소드가 있는지 확인해주세요. 없다면 이 라인은 주석 처리하거나 Seller 엔티티를 수정해야 합니다. ===
            // seller.setUpdatedAt(LocalDateTime.now());
            sellerRepository.save(seller);
            log.info("Seller (ID: {}) has been set to inactive.", seller.getId());

            List<SellerReview> sellerReceivedReviews = sellerReviewRepository.findAllBySellerId(seller.getId());
            if (sellerReceivedReviews != null && !sellerReceivedReviews.isEmpty()) {
                log.info("Hiding {} seller reviews received by seller ID: {}", sellerReceivedReviews.size(), seller.getId());
                for (SellerReview review : sellerReceivedReviews) {
                    review.setIsHidden(true);
                }
                sellerReviewRepository.saveAll(sellerReceivedReviews);
            }

            /* 판매자 상품 판매 중지 로직 (ProductRepository 필요)
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

    /**
     * 특정 ID의 고객 상세 정보를 조회합니다.
     *
     * @param customerId 조회할 고객 ID
     * @return 고객 상세 정보 (CustomerDetailDTO)
     * @throws EntityNotFoundException 해당 ID의 고객을 찾을 수 없는 경우
     */
    @Override // AdminUserService 인터페이스의 메소드를 오버라이드
    @Transactional(readOnly = true)
    public CustomerDetailDTO getCustomerDetails(Long customerId)
            throws EntityNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        return convertToCustomerDetailDTO(customer);
    }

    /**
     * 특정 ID의 판매자 상세 정보를 조회합니다.
     *
     * @param sellerId 조회할 판매자 ID
     * @return 판매자 상세 정보 (SellerDetailDTO)
     * @throws EntityNotFoundException 해당 ID의 판매자를 찾을 수 없는 경우
     */
    @Override // AdminUserService 인터페이스의 메소드를 오버라이드
    @Transactional(readOnly = true)
    public SellerDetailDTO getSellerDetails(Long sellerId)
            throws EntityNotFoundException {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
        return convertToSellerDetailDTO(seller);
    }

    // DTO 변환 메소드들
    private UserManagementListItemDTO convertToUserManagementDTO(Customer customer) {
        return UserManagementListItemDTO.builder()
                .id(customer.getId())
                .userType("CUSTOMER")
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreated()) // Customer 엔티티의 생성일 필드 getter (예: getCreated() 또는 getCreatedAt())
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
                .createdAt(seller.getCreatedAt()) // Seller 엔티티의 생성일 필드 getter
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
                .createdAt(customer.getCreated())  // Customer 엔티티의 생성일 필드 getter
                .updatedAt(customer.getUpdated())  // Customer 엔티티의 수정일 필드 getter
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
                .createdAt(seller.getCreatedAt()) // Seller 엔티티의 생성일 필드 getter
                .updatedAt(seller.getUpdatedAt()) // Seller 엔티티의 수정일 필드 getter
                .build();
    }
}
