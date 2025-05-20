package com.realive.repository.productview;

import java.util.List;

import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;

import com.realive.dto.product.ProductListDto;

public interface ProductSearch {
    List <ProductListDto> productList(Pageable pageable);
}
