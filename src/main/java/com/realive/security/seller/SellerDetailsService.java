package com.realive.security.seller; // 실제 프로젝트 경로에 맞게 수정

import com.realive.domain.seller.Seller;
import com.realive.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("sellerDetailsService")
@RequiredArgsConstructor
public class SellerDetailsService implements UserDetailsService {

    private final SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Seller seller = sellerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("판매자를 찾을 수 없습니다: " + username));

        return new SellerPrincipal(seller);
    }
}