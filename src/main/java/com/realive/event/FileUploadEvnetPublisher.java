package com.realive.event;

import com.realive.domain.seller.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileUploadEvnetPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(Seller seller, MultipartFile licensFile, MultipartFile bankFile){
        publisher.publishEvent(new SellerFileUploadEvent(seller, licensFile, bankFile));
    }
    
}
