//package com.realive.serviceimpl.admin.user;
//
//import com.realive.domain.customer.CustomerQna;
//import com.realive.dto.customer.customerqna.CustomerQnaDetailDTO;
//import com.realive.dto.customer.customerqna.CustomerQnaListDTO;
//import com.realive.repository.customer.CustomerQnaRepository;
//import com.realive.service.admin.user.AdminQnaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import jakarta.persistence.EntityNotFoundException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
//public class AdminQnaServiceImpl implements AdminQnaService {
//
//    private final CustomerQnaRepository customerQnaRepository;
//
//    @Override
//    public Page<CustomerQnaListDTO> getAllCustomerQnaList(Pageable pageable) {
//        Page<CustomerQna> qnaPage = customerQnaRepository.findAll(pageable); // 모든 고객 Q&A 조회
//        return qnaPage.map(qna -> CustomerQnaListDTO.builder()
//                .id(qna.getId())
//                .title(qna.getTitle())
//                .createdAt(qna.getCreatedAt())
//                .isAnswered(qna.getIsAnswered())
//                .build());
//    }
//
//    @Override
//    public CustomerQnaDetailDTO getCustomerQnaDetail(Long qnaId) {
//        CustomerQna qna = customerQnaRepository.findById(qnaId)
//                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다."));
//
//        return CustomerQnaDetailDTO.builder()
//                .id(qna.getId())
//                .title(qna.getTitle())
//                .content(qna.getContent())
//                .answer(qna.getAnswer())
//                .isAnswered(qna.getIsAnswered())
//                .createdAt(qna.getCreatedAt())
//                .updatedAt(qna.getUpdatedAt())
//                .answeredAt(qna.getAnsweredAt())
//                .build();
//    }
//
//    @Override
//    public Page<CustomerQnaListDTO> getUnansweredCustomerQnaList(Pageable pageable) {
//        Page<CustomerQna> qnaPage = customerQnaRepository.findByIsAnsweredFalse(pageable);
//        return qnaPage.map(qna -> CustomerQnaListDTO.builder()
//                .id(qna.getId())
//                .title(qna.getTitle())
//                .createdAt(qna.getCreatedAt())
//                .isAnswered(qna.getIsAnswered())
//                .build());
//    }
//
//    @Override
//    public List<CustomerQnaListDTO> getCustomerQnaListByProduct(Long productId) {
//        List<CustomerQna> qnaList = customerQnaRepository.findByProductIdOrderByIdDesc(productId);
//        return qnaList.stream()
//                .map(qna -> CustomerQnaListDTO.builder()
//                        .id(qna.getId())
//                        .title(qna.getTitle())
//                        .createdAt(qna.getCreatedAt())
//                        .isAnswered(qna.getIsAnswered())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public void deleteCustomerQna(Long qnaId) {
//        CustomerQna qna = customerQnaRepository.findById(qnaId)
//                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다: " + qnaId));
//        customerQnaRepository.delete(qna);
//    }
//}