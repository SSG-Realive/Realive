package com.realive.event;

import com.realive.domain.seller.Seller;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@RequiredArgsConstructor
public class SellerFileUploadEvent {
    
    private final Seller seller;
    private final MultipartFile licensFile;
    private final MultipartFile bankFile;
    
}