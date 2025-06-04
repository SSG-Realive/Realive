package com.realive.serviceimpl.admin.product;

import com.realive.domain.product.Product;
import com.realive.dto.admin.ProductDetailDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.product.ProductRepository;
import com.realive.service.admin.product.ProductManagementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductListDTO> getProducts(Long sellerId, String search, Boolean active, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (sellerId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("seller").get("id"), sellerId)
            );
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("name"), "%" + search + "%")
            );
        }
        if (active != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("active"), active)
            );
        }

        return productRepository.findAll(spec, pageable)
                .map(product -> ProductListDTO.from(product, null));
    }

    @Override
    public ProductDetailDTO getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductDetailDTO.from(product, null);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
}