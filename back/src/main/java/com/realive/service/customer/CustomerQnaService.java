package com.realive.service.customer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.CustomerQna;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.customer.customerqna.CustomerQnaDetailDTO;
import com.realive.dto.customer.customerqna.CustomerQnaListDTO;
import com.realive.dto.customer.customerqna.CustomerQnaRequestDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.customer.CustomerQnaRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.customer.productview.ProductListRepository;
import com.realive.repository.customer.productview.ProductViewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

// [Customer] Q&A Service

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerQnaService {

    private final ProductViewRepository ProductViewRepository;
    private final CustomerQnaRepository customerQnaRepository;
    private final CustomerRepository customerRepository;
    private final ProductListRepository productListRepository;

    // 판매 상품 1:1 문의하기 + 상단에 상품 요약
    public Map<String, Object> createQnaWithProductSummary(CustomerQnaRequestDTO requestDTO) {

        Product product = ProductViewRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID=" + requestDTO.getProductId()));

        Customer customer = customerRepository.findActiveUserById(requestDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다. ID=" + requestDTO.getCustomerId()));

        Seller seller = product.getSeller();

        CustomerQna qna = CustomerQna.builder()
                .product(product)
                .seller(seller)
                .customer(customer)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .isAnswered(false)
                .build();

        CustomerQna saved = customerQnaRepository.save(qna);

        // 상품 요약 정보 가져오기
        ProductListDTO productSummary = productListRepository
                .getWishlistedProducts(List.of(product.getId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품 요약 정보를 찾을 수 없습니다."));

        return Map.of(
                "qnaId", saved.getId(),
                "product", productSummary
        );
    }

    // 내 문의 목록 조회
    public List<Map<String, Object>> listQnaWithProductSummary(Long customerId) {

        List<CustomerQna> qnaList = customerQnaRepository.findByCustomerIdOrderByIdDesc(customerId);

        List<Long> productIds = qnaList.stream()
                .map(q -> q.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ProductListDTO> productSummaries = productListRepository.getWishlistedProducts(productIds);
        Map<Long, ProductListDTO> productMap = productSummaries.stream()
                .collect(Collectors.toMap(ProductListDTO::getId, dto -> dto));

        List<Map<String, Object>> resultList = qnaList.stream().map(qna -> {
            Map<String, Object> result = new HashMap<>();

            CustomerQnaListDTO qnaDto = CustomerQnaListDTO.builder()
                    .id(qna.getId())
                    .title(qna.getTitle())
                    .createdAt(qna.getCreatedAt())
                    .isAnswered(qna.getIsAnswered())
                    .build();

            result.put("qna", qnaDto);
            result.put("productSummary", productMap.getOrDefault(qna.getProduct().getId(), null));

            return result;
        }).collect(Collectors.toList());

        return resultList;
    }

    // 문의 상세조회(문의+답변)
    public Map<String, Object> detailQnaWithProductSummary(Long id, Long customerId) {

        CustomerQna qna = customerQnaRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new EntityNotFoundException("문의 정보를 찾을 수 없습니다. ID=" + id));

        Product product = qna.getProduct();

        ProductListDTO productSummary = productListRepository
                .getWishlistedProducts(List.of(product.getId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품 요약 정보를 찾을 수 없습니다."));

        CustomerQnaDetailDTO qnaDetail = CustomerQnaDetailDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.getIsAnswered())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt()) // nullable 가능
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("qna", qnaDetail);
        result.put("productSummary", productSummary);

        return result;
    }

    // 상품 문의 목록 조회(상품 상세조회 페이지)
    public List<CustomerQnaListDTO> listProductQnaWith(Long productId) {

        List<CustomerQna> qnaList = customerQnaRepository.findByProductIdOrderByIdDesc(productId);

        return qnaList.stream()
                .map(qna -> CustomerQnaListDTO.builder()
                        .id(qna.getId())
                        .title(qna.getTitle())
                        .createdAt(qna.getCreatedAt())
                        .isAnswered(qna.getIsAnswered())
                        .build())
                .collect(Collectors.toList());
    }

    // 추가: 판매자의 상품에 대한 고객 문의 목록 조회
    public List<Map<String, Object>> listSellerProductQna(Long sellerId) {
        List<CustomerQna> qnaList = customerQnaRepository.findBySellerIdOrderByIdDesc(sellerId);

        List<Long> productIds = qnaList.stream()
                .map(q -> q.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ProductListDTO> productSummaries = productListRepository.getWishlistedProducts(productIds);
        Map<Long, ProductListDTO> productMap = productSummaries.stream()
                .collect(Collectors.toMap(ProductListDTO::getId, dto -> dto));

        List<Map<String, Object>> resultList = qnaList.stream().map(qna -> {
            Map<String, Object> result = new HashMap<>();

            CustomerQnaListDTO qnaDto = CustomerQnaListDTO.builder()
                    .id(qna.getId())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .createdAt(qna.getCreatedAt())
                    .isAnswered(qna.getIsAnswered())
                    .customerName(qna.getCustomer().getName())
                    .productName(qna.getProduct().getName())
                    .productId(qna.getProduct().getId())
                    .build();

            result.put("qna", qnaDto);
            result.put("productSummary", productMap.getOrDefault(qna.getProduct().getId(), null));

            return result;
        }).collect(Collectors.toList());

        return resultList;
    }

    // 추가: 판매자가 고객 문의 상세 조회
    public Map<String, Object> detailSellerProductQna(Long qnaId, Long sellerId) {
        CustomerQna qna = customerQnaRepository.findByIdAndSellerId(qnaId, sellerId)
                .orElseThrow(() -> new EntityNotFoundException("문의 정보를 찾을 수 없습니다. ID=" + qnaId));

        Product product = qna.getProduct();

        ProductListDTO productSummary = productListRepository
                .getWishlistedProducts(List.of(product.getId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품 요약 정보를 찾을 수 없습니다."));

        CustomerQnaDetailDTO qnaDetail = CustomerQnaDetailDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.getIsAnswered())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt())
                .customerName(qna.getCustomer().getName())
                .productName(qna.getProduct().getName())
                .productId(qna.getProduct().getId())
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("qna", qnaDetail);
        result.put("productSummary", productSummary);

        return result;
    }

    // 수정: 새로운 객체 생성 방법으로 답변하기
    public void answerCustomerQna(Long qnaId, Long sellerId, String answer) {
        CustomerQna qna = customerQnaRepository.findByIdAndSellerId(qnaId, sellerId)
                .orElseThrow(() -> new EntityNotFoundException("문의 정보를 찾을 수 없습니다. ID=" + qnaId));

        // 새로운 CustomerQna 객체 생성하여 답변 정보 설정
        CustomerQna updatedQna = CustomerQna.builder()
                .id(qna.getId())                    // 기존 ID 그대로 사용
                .seller(qna.getSeller())            // 기존 판매자 그대로
                .product(qna.getProduct())          // 기존 상품 그대로
                .customer(qna.getCustomer())        // 기존 고객 그대로
                .title(qna.getTitle())              // 기존 제목 그대로
                .content(qna.getContent())          // 기존 내용 그대로
                .answer(answer)                     // 새로운 답변 설정
                .isAnswered(true)                   // 답변 완료로 설정
                .answeredAt(LocalDateTime.now())    // 답변 시간 설정
                .createdAt(qna.getCreatedAt())      // 기존 생성시간 그대로
                .build();

        customerQnaRepository.save(updatedQna);
    }

    // 추가: 판매자용 페이징 메서드들
    public Page<CustomerQna> getSellerQnaPage(Pageable pageable, Long sellerId) {
        return customerQnaRepository.findBySellerId(pageable, sellerId);
    }

    public Page<CustomerQna> getSellerUnansweredQnaPage(Pageable pageable, Long sellerId) {
        return customerQnaRepository.findBySellerIdAndIsAnsweredFalse(pageable, sellerId);
    }

    // 추가: 페이징된 판매자 문의 목록을 DTO로 변환
    public Page<Map<String, Object>> getSellerQnaPageWithProductSummary(Pageable pageable, Long sellerId) {
        Page<CustomerQna> qnaPage = customerQnaRepository.findBySellerId(pageable, sellerId);

        List<Long> productIds = qnaPage.getContent().stream()
                .map(q -> q.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ProductListDTO> productSummaries = productListRepository.getWishlistedProducts(productIds);
        Map<Long, ProductListDTO> productMap = productSummaries.stream()
                .collect(Collectors.toMap(ProductListDTO::getId, dto -> dto));

        Page<Map<String, Object>> resultPage = qnaPage.map(qna -> {
            Map<String, Object> result = new HashMap<>();

            CustomerQnaListDTO qnaDto = CustomerQnaListDTO.builder()
                    .id(qna.getId())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .createdAt(qna.getCreatedAt())
                    .isAnswered(qna.getIsAnswered())
                    .customerName(qna.getCustomer().getName())
                    .productName(qna.getProduct().getName())
                    .productId(qna.getProduct().getId())
                    .build();

            result.put("qna", qnaDto);
            result.put("productSummary", productMap.getOrDefault(qna.getProduct().getId(), null));

            return result;
        });

        return resultPage;
    }
}