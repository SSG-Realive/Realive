package com.realive.event;

import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SellerFileUploadEvent {
    
    private final Seller seller;
    private final MultipartFile licensFile;
    private final MultipartFile bankFile;
    
}