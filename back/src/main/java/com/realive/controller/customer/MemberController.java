package com.realive.controller.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.customer.member.MemberJoinDTO;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.dto.customer.member.MemberReadDTO;
import com.realive.service.customer.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] 회원 관련 컨트롤러

@Log4j2
@RestController
@RequestMapping("/api/customer/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 임시회원: 소셜 로그인 후 임시 회원 회원으로 전환
    @PutMapping("/update-info")
    public ResponseEntity<?> updateTemporaryUserInfo(
            @RequestBody MemberJoinDTO request,
            Authentication authentication) {

        log.info("updateTemporaryUserInfo 호출됨");
        log.info("Authentication: {}", authentication);

        // 로그인 사용자 이메일 가져오기
        String currentEmail = authentication.getName();

        // 이메일이 요청과 다르면 권한 문제일 수 있음 (보안 검증)
        if (!currentEmail.equals(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("본인의 정보만 수정할 수 있습니다.");
        }

        try {
            memberService.updateTemporaryUserInfo(request, currentEmail);
            return ResponseEntity.ok("회원정보가 정상적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("회원정보 수정 중 오류가 발생했습니다.");
        }
    }

    // 회원정보조회
    @GetMapping("/me")
    public ResponseEntity<MemberReadDTO> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        MemberReadDTO profile = memberService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    // 회원정보수정
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(@AuthenticationPrincipal MemberLoginDTO loginDTO,
                                          @RequestBody MemberReadDTO updateDTO) {
        memberService.updateMember(loginDTO.getEmail(), updateDTO);
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<String> delectMember(Authentication authentication){
        String email = authentication.getName();
        memberService.deactivateByEmail(email);
        return ResponseEntity.ok("회원 탈퇴가 정상 처리되었습니다.");
    }

}

