package com.realive.service.productview;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;
import com.realive.repository.productview.ProductSearch;

import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductSearch productSearch;

    public ProductViewServiceImpl(@Qualifier("productSearchImpl") ProductSearch productSearch) {
        this.productSearch = productSearch;
    }

    @Override
    public PageResponseDTO<ProductListDto> search(PageRequestDTO dto, Long categoryId) {
        return productSearch.search(dto, categoryId);
    }
}


