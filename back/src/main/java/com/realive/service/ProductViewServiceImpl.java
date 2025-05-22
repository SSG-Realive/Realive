package com.realive.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.repository.productview.ProductViewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductViewRepository productViewRepository;
    
    @Override
    public Page<ProductListDTO> getProductList(Pageable pageable) {
    List<ProductListDTO> dtoList = productViewRepository.productList(pageable);
    long total = productViewRepository.count();

    return new PageImpl<>(dtoList, pageable, total);
    }

    @Override
    public ProductResponseDTO getProductDetail(Long id) {
        return productViewRepository.findProductDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 상품이 존재하지 않습니다. id=" + id));
    }


}
