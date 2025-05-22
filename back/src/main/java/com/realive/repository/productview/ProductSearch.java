package com.realive.repository.productview;

import java.util.List;

import org.springframework.data.domain.Pageable;
import com.realive.dto.product.ProductListDTO;


public interface ProductSearch {
    List <ProductListDTO> productList(Pageable pageable);
}
