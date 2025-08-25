package com.realive.controller.admin.user;


import com.realive.dto.admin.user.CustomerDetailDTO; // 추가
import com.realive.dto.admin.user.SellerDetailDTO;   // 추가
import com.realive.dto.admin.user.UpdateUserStatusRequestDTO;
import com.realive.dto.admin.user.UserManagementListItemDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.service.admin.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException; // 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin: User Management", description = "관리자 사용자 관리 관련 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "전체 사용자 목록 조회") // (설명 이전과 동일)
    @ApiResponses(value = { /* 이전과 동일 */ })
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserManagementListItemDTO>>> getAllUsers(
            @PageableDefault(size = 10, sort = "id,asc") Pageable pageable,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isActive) {
        // (이전 최종본의 getAllUsers API 내용과 동일 - 생략)
        log.info("GET /api/admin/users - page: {}, size: {}, sort: {}, userType: {}, searchTerm: {}, isActive: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), userType, searchTerm, isActive);
        try {
            Page<UserManagementListItemDTO> userPage = adminUserService.getAllUsers(
                    pageable,
                    Optional.ofNullable(userType),
                    Optional.ofNullable(searchTerm),
                    Optional.ofNullable(isActive)
            );
            return ResponseEntity.ok(ApiResponse.success(userPage));
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "사용자 목록 조회 중 서버 오류 발생: " + e.getMessage()));
        }
    }

    @Operation(summary = "사용자 상태 변경") // (설명 이전과 동일)
    @ApiResponses(value = { /* 이전과 동일 */ })
    @PutMapping("/users/{userTypePath}/{userId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable String userTypePath,
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequestDTO requestDTO) {
        // (이전 최종본의 updateUserStatus API 내용과 동일 - 생략)
        log.info("PUT /api/admin/users/{}/{}/status - new isActive: {}", userTypePath, userId, requestDTO.getIsActive());
        try {
            String serviceUserType;
            if ("customers".equalsIgnoreCase(userTypePath)) {
                serviceUserType = "CUSTOMER";
            } else if ("sellers".equalsIgnoreCase(userTypePath)) {
                serviceUserType = "SELLER";
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid user type in URL path. Must be 'customers' or 'sellers'."));
            }

            if (requestDTO.getIsActive() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "'isActive' field is required in the request body and must be true or false."));
            }

            adminUserService.updateUserStatus(userId, serviceUserType, requestDTO.getIsActive());
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully."));

        } catch (EntityNotFoundException e) { /* ... */ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));}
        catch (IllegalArgumentException e) { /* ... */ return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));}
        catch (Exception e) { /* ... */ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Server error during status update."));}
    }


    @Operation(summary = "사용자 물리적 삭제") // (설명 이전과 동일)
    @ApiResponses(value = { /* 이전과 동일 */ })
    @DeleteMapping("/users/{userTypePath}/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String userTypePath,
            @PathVariable Long userId) {
        // (이전 최종본의 deleteUser API 내용과 동일 - 생략)
        log.info("DELETE /api/admin/users/{}/{}", userTypePath, userId);
        try {
            String serviceUserType;
            if ("customers".equalsIgnoreCase(userTypePath)) {
                serviceUserType = "CUSTOMER";
            } else if ("sellers".equalsIgnoreCase(userTypePath)) {
                serviceUserType = "SELLER";
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(),"Invalid user type in URL path. Must be 'customers' or 'sellers'."));
            }

            adminUserService.deleteUser(userId, serviceUserType);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) { /* ... */ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));}
        catch (IllegalArgumentException e) { /* ... */ return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));}
        catch (DataIntegrityViolationException e) { /* ... */ return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(HttpStatus.CONFLICT.value(), e.getMessage()));}
        catch (Exception e) { /* ... */ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Server error during user deletion."));}
    }

    @Operation(summary = "고객 상세 정보 조회", description = "특정 고객의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/customers/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDetailDTO>> getCustomerDetails(
            @Parameter(description = "고객 ID", example = "1", required = true)
            @PathVariable Long customerId) {
        log.info("GET /api/admin/users/customers/{}", customerId);
        try {
            CustomerDetailDTO customerDetails = adminUserService.getCustomerDetails(customerId);
            return ResponseEntity.ok(ApiResponse.success(customerDetails));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Server error fetching customer details."));
        }
    }

    @Operation(summary = "판매자 상세 정보 조회", description = "특정 판매자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SellerDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users/sellers/{sellerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SellerDetailDTO>> getSellerDetails(
            @Parameter(description = "판매자 ID", example = "1", required = true)
            @PathVariable Long sellerId) {
        log.info("GET /api/admin/users/sellers/{}", sellerId);
        try {
            SellerDetailDTO sellerDetails = adminUserService.getSellerDetails(sellerId);
            return ResponseEntity.ok(ApiResponse.success(sellerDetails));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Server error fetching seller details."));
        }
    }

    // --- Swagger 응답 스키마 정의를 위한 내부 정적 클래스 ---
    private static class UserManagementListPageApiResponse extends ApiResponse<Page<UserManagementListItemDTO>> {}
    private static class StringApiResponse extends ApiResponse<String> {}
    private static class CustomerDetailApiResponse extends ApiResponse<CustomerDetailDTO> {}
    private static class SellerDetailApiResponse extends ApiResponse<SellerDetailDTO> {}
}
