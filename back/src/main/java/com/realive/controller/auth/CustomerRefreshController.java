package com.realive.controller.auth; // RefreshController와 같은 패키지

import com.realive.domain.customer.Customer;
import com.realive.dto.customer.login.CustomerLoginResponseDTO;
// Customer용 응답 DTO
import com.realive.exception.UnauthorizedException;
import com.realive.repository.customer.CustomerRepository; // Customer용 Repository
import com.realive.security.JwtUtil;
import com.realive.security.customer.CustomerPrincipal; // Customer용 Principal
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// ✅ Customer 전용 리프레시 경로를 지정합니다.
@RequestMapping("/api/auth/customer") 
@RequiredArgsConstructor
public class CustomerRefreshController {

    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository; // ✅ CustomerRepository 주입

    @PostMapping("/refresh")
    public ResponseEntity<CustomerLoginResponseDTO> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않습니다.");
        }

        String subject = jwtUtil.getSubjectFromToken(refreshToken);
        // ✅ Customer 리프레시 토큰이 맞는지 확인합니다.
        if (!JwtUtil.SUBJECT_CUSTOMER_REFRESH.equals(subject)) {
            throw new UnauthorizedException("고객용 리프레시 토큰이 아닙니다.");
        }

        // ✅ 토큰에서 ID를 추출하여 Customer를 조회합니다.
        Long customerId = jwtUtil.getUserIdFromToken(refreshToken); // getUserIdFromToken 재사용
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("고객 정보를 찾을 수 없습니다"));

        // ✅ Customer 엔티티로 CustomerPrincipal 객체를 생성합니다.
        CustomerPrincipal principal = new CustomerPrincipal(customer);

        // ✅ Principal로 새로운 Access Token을 생성합니다.
        String newAccessToken = jwtUtil.generateAccessToken(principal);

        // ✅ Customer용 응답 DTO를 생성하여 반환합니다.
        CustomerLoginResponseDTO dto = CustomerLoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .email(customer.getEmail())
                .name(customer.getName())
                .build();

        return ResponseEntity.ok(dto);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request){
        if (request.getCookies() == null) return null;
        for(Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) { // 쿠키 이름은 통일
                return cookie.getValue();
            }
        }
        return null;
    }
}