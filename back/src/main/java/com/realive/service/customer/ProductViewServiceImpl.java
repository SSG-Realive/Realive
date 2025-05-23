package com.realive.service.productview;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductResponseDto;
import com.realive.repository.productview.ProductDetail;
import com.realive.repository.productview.ProductSearch;
import com.realive.service.customer.ProductViewService;

import lombok.extern.log4j.Log4j2;

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
    public PageResponseDTO<ProductListDto> search(PageRequestDTO dto, Long categoryId) {
        return productSearch.search(dto, categoryId);
    }

    @Override
    public ProductResponseDto getProductDetail(Long id) {
        return productDetail.findProductDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 상품이 존재하지 않습니다. id=" + id));
    }

}


