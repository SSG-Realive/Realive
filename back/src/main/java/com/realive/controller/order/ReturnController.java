package com.realive.controller.order;

import com.realive.dto.order.ReturnProcessRequestDTO;
import com.realive.dto.order.ReturnRequestDTO;
import com.realive.dto.order.ReturnResponseDTO;
import com.realive.service.order.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Log4j2
public class ReturnController {

    private final ReturnService returnService;

    // TODO: 실제 SecurityContextHolder에서 사용자 ID를 가져오는 로직으로 대체 필요
    // 개발 편의를 위해 임시로 제공하는 메서드 (실제 배포 시에는 반드시 제거 또는 교체)
    private Long getAuthenticatedCustomerId() {
        // 실제 Security 적용 시:
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        //     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //     // UserDetails에 customerId가 있다면 해당 값을 반환
        //     // 없다면, 사용자명(username)을 통해 고객 엔티티를 조회하여 ID 반환
        //     // 예: return ((CustomerUserDetails) userDetails).getCustomerId();
        // }
        return 1L; // 임시 customerId
    }

    // TODO: 관리자 권한 확인 로직도 실제 Security를 활용해야 함
    private boolean isAdmin() {
        // 실제 Security 적용 시:
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // return authentication != null && authentication.getAuthorities().stream()
        //        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return true; // 임시 관리자 권한
    }


    /**
     * [고객] 반품 요청 생성
     * POST /api/returns
     * @param requestDTO 반품 요청 DTO (JSON part)
     * @param evidenceImages 하자 증빙 이미지 파일들 (MultipartFile part, 선택 사항)
     * @return 생성된 반품 요청 상세 정보 (201 Created)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 파일 업로드를 위해 MediaType.MULTIPART_FORM_DATA_VALUE 지정
    public ResponseEntity<ReturnResponseDTO> createReturnRequest(
            @Valid @RequestPart("request") ReturnRequestDTO requestDTO, // JSON 데이터를 위한 @RequestPart
            @RequestPart(value = "evidenceImages", required = false) List<MultipartFile> evidenceImages) { // 파일 데이터를 위한 @RequestPart
        log.info("Received return request: {}", requestDTO);

        // DTO에 이미지 파일 목록 설정 (서비스로 전달하기 위함)
        requestDTO.setEvidenceImages(evidenceImages);

        Long customerId = getAuthenticatedCustomerId(); // 인증된 사용자 ID
        ReturnResponseDTO response = returnService.createReturnRequest(customerId, requestDTO);
        log.info("Return request created successfully: {}", response.getReturnRequestId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * [고객] 특정 반품 요청 상세 조회
     * GET /api/returns/{returnRequestId}
     * @param returnRequestId 조회할 반품 요청 ID
     * @return 반품 요청 상세 정보 (200 OK)
     */
    @GetMapping("/{returnRequestId}")
    public ResponseEntity<ReturnResponseDTO> getReturnRequest(@PathVariable Long returnRequestId) {
        log.info("Request to get return request details for ID: {}", returnRequestId);
        Long customerId = getAuthenticatedCustomerId(); // 인증된 사용자 ID
        ReturnResponseDTO response = returnService.getReturnRequest(customerId, returnRequestId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * [고객] 본인의 모든 반품 요청 목록 조회
     * GET /api/returns
     * @return 반품 요청 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<ReturnResponseDTO>> getReturnRequestsByCustomer() {
        log.info("Request to get all return requests for customer.");
        Long customerId = getAuthenticatedCustomerId(); // 인증된 사용자 ID
        List<ReturnResponseDTO> response = returnService.getReturnRequestsByCustomer(customerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * [관리자] 반품 요청 처리 (상태 변경, 메모 추가, 환불 금액 설정 등)
     * PUT /api/returns/{returnRequestId}/process
     * @param returnRequestId 처리할 반품 요청 ID
     * @param requestDTO 처리 정보 (새 상태, 관리자 메모, 환불 금액 등)
     * @return 처리된 반품 요청 상세 정보 (200 OK)
     */
    @PutMapping("/{returnRequestId}/process")
    public ResponseEntity<ReturnResponseDTO> processReturnRequest(
            @PathVariable Long returnRequestId,
            @Valid @RequestBody ReturnProcessRequestDTO requestDTO) {
        log.info("Request to process return request {}: {}", returnRequestId, requestDTO);

        // 관리자 권한 확인 (실제 서비스에서는 보안 프레임워크를 통해 처리)
        if (!isAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        ReturnResponseDTO response = returnService.processReturnRequest(returnRequestId, requestDTO);
        log.info("Return request {} processed. New status: {}", returnRequestId, response.getStatusDescription());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 예외 처리 핸들러 (GlobalExceptionHandler 또는 개별 컨트롤러에 추가 가능)
    // @ExceptionHandler({IllegalArgumentException.class, EntityNotFoundException.class, IllegalStateException.class})
    // public ResponseEntity<String> handleReturnExceptions(RuntimeException ex) {
    //     log.error("Return process exception: {}", ex.getMessage());
    //     if (ex instanceof IllegalArgumentException) {
    //         return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
    //     } else if (ex instanceof EntityNotFoundException) {
    //         return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
    //     } else if (ex instanceof IllegalStateException) {
    //         return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    //     }
    //     return new ResponseEntity<>("Internal server error during return process.", HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    // }
}