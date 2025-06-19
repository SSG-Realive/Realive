package com.realive.controller.customer;

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
import com.realive.dto.customer.member.MemberModifyDTO;
import com.realive.dto.customer.member.MemberReadDTO;
import com.realive.exception.UnauthorizedException;
import com.realive.security.customer.CustomerPrincipal;
import com.realive.service.customer.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] 회원 관련 컨트롤러

@Log4j2
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 임시회원: 소셜 로그인 후 임시회원으로 전회원을 회원으로 전환
    @PutMapping("/update-info")
    public ResponseEntity<?> updateTemporaryUserInfo(
            @RequestBody @Valid MemberJoinDTO request,
            Authentication authentication) {

        log.info("updateTemporaryUserInfo 호출됨");
        log.info("Authentication: {}", authentication);

        // 로그인 사용자 이메일 가져오기
        String currentEmail = authentication.getName();

        // 권한 검증: 본인만 수정 가능
        if (!currentEmail.equals(request.getEmail())) {
            throw new UnauthorizedException("본인의 정보만 수정할 수 있습니다.");
        }

        memberService.updateTemporaryUserInfo(request, currentEmail);
        return ResponseEntity.ok("회원정보가 정상적으로 수정되었습니다.");
    }

    // 회원정보조회
    @GetMapping("/mypage")
    public ResponseEntity<MemberReadDTO> getMyProfile(@AuthenticationPrincipal CustomerPrincipal principal) {

        String email = principal.getEmail();
        MemberReadDTO profile = memberService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/mypage")
    public ResponseEntity<Void> updateMyInfo(
            @AuthenticationPrincipal CustomerPrincipal principal, // ✅
            @RequestBody @Valid MemberModifyDTO updateDTO) {

        memberService.updateMember(principal.getEmail(), updateDTO); // principal에서 바로 이메일
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @DeleteMapping("/mypage")
    public ResponseEntity<String> deleteMember( // ✅ 메서드명 수정
            @AuthenticationPrincipal CustomerPrincipal principal) { // ✅

        memberService.deactivateByEmail(principal.getEmail());
        return ResponseEntity.ok("회원 탈퇴가 정상 처리되었습니다.");
    }
}
