package com.realive.service.admin.user;


import com.realive.dto.admin.user.CustomerDetailDTO;
import com.realive.dto.admin.user.SellerDetailDTO;
import com.realive.dto.admin.user.UserManagementListItemDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AdminUserService {

    Page<UserManagementListItemDTO> getAllUsers(
            Pageable pageable,
            Optional<String> userTypeFilter,
            Optional<String> searchTerm,
            Optional<Boolean> activeFilter
    );

    boolean updateUserStatus(Long userId, String userType, boolean newIsActive)
            throws EntityNotFoundException, IllegalArgumentException;

    void deleteUser(Long userId, String userType)
            throws EntityNotFoundException, IllegalArgumentException, DataIntegrityViolationException;

    /**
     * 특정 고객의 상세 정보를 조회합니다.
     * @param customerId 고객 ID
     * @return 고객 상세 정보 DTO
     * @throws EntityNotFoundException 해당 ID의 고객이 없을 경우
     */
    CustomerDetailDTO getCustomerDetails(Long customerId)
            throws EntityNotFoundException;

    /**
     * 특정 판매자의 상세 정보를 조회합니다.
     * @param sellerId 판매자 ID
     * @return 판매자 상세 정보 DTO
     * @throws EntityNotFoundException 해당 ID의 판매자가 없을 경우
     */
    SellerDetailDTO getSellerDetails(Long sellerId)
            throws EntityNotFoundException;
}
