package com.realive.service.customer;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.repository.customer.productview.ProductDetail;
import com.realive.repository.customer.productview.ProductSearch;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

// [Customer] 상품 조회 Service 구현체

@Service
@Transactional
@Log4j2
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductSearch productSearch;
    private final ProductDetail productDetail;

    public ProductViewServiceImpl(
            @Qualifier("productSearchImpl") ProductSearch productSearch,
            @Qualifier("productDetailImpl") ProductDetail productDetail) {
        this.productSearch = productSearch;
        this.productDetail = productDetail;
    }

    @Override
    public PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId) {
        return productSearch.search(dto, categoryId);
    }

    @Override
    public ProductResponseDTO getProductDetail(Long id) {
        return productDetail.findProductDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + id));
    }

}
