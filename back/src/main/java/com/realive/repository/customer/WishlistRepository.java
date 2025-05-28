package com.realive.repository.customer;

import java.util.List;
import java.util.Optional;

import com.realive.domain.customer.Customer;
import com.realive.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.Wishlist;

// 찜 레포지토리

public interface WishlistRepository extends JpaRepository<Wishlist, Long>, WishlistRepositoryCustom  {

    //찜
    @Query("SELECT w FROM Wishlist w WHERE w.customer.id = :customerId AND w.product.id = :productId")
    Optional<Wishlist> findByCustomerIdAndProductId(@Param("customerId") Long customerId,@Param("productId") Long productId);

    //찜 목록 조회용 상품id 뽑기
    @Query("SELECT w.product.id FROM Wishlist w WHERE w.customer.id = :customerId ORDER BY w.created DESC")
    List<Long> findProductIdsByCustomerId(@Param("customerId") Long customerId);

    Optional<Wishlist> findByCustomerAndProduct(Customer customer, Product product);
    void deleteByCustomerAndProduct(Customer customer, Product product);
    
}
