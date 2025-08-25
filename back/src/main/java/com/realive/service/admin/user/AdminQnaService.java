package com.realive.service.admin.user;

import com.realive.dto.customer.customerqna.CustomerQnaDetailDTO;
import com.realive.dto.customer.customerqna.CustomerQnaListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminQnaService {

    // 전체 고객 Q&A 목록 조회
    Page<CustomerQnaListDTO> getAllCustomerQnaList(Pageable pageable);

    // 특정 고객 Q&A 상세 조회
    CustomerQnaDetailDTO getCustomerQnaDetail(Long qnaId);

    // [옵션] 답변되지 않은 고객 Q&A 목록 조회
    Page<CustomerQnaListDTO> getUnansweredCustomerQnaList(Pageable pageable);

    // [옵션] 특정 상품에 대한 고객 Q&A 목록 조회
    List<CustomerQnaListDTO> getCustomerQnaListByProduct(Long productId);

    // 고객 Q&A 삭제
    void deleteCustomerQna(Long qnaId);
}