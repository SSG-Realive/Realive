//package com.realive.serviceimpl.seller;
//
//import com.realive.domain.seller.Seller;
//import com.realive.domain.seller.SellerQna;
//import com.realive.domain.customer.Customer;
//import com.realive.dto.seller.*;
//import com.realive.repository.seller.SellerQnaRepository;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.repository.user.CustomerRepository;
//import com.realive.service.seller.SellerQnaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class SellerQnaServiceImpl implements SellerQnaService {
//
//    private final SellerQnaRepository sellerQnaRepository;
//    private final SellerRepository sellerRepository;
//    private final CustomerRepository customerRepository;
//
//    /**
//     * 판매자 이메일로 QnA 목록 조회 (isActive = true 조건)
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<SellerQnaResponseDTO> getQnaListByEmail(String email, Pageable pageable) {
//        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(email)
//                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
//        Page<SellerQna> qnas = sellerQnaRepository.findBySellerIdAndIsActiveTrue(seller.getId(), pageable);
//        return qnas.map(SellerQnaResponseDTO::fromEntity);
//    }
//
//    /**
//     * QnA 상세 조회 (삭제되지 않은 항목만)
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public SellerQnaDetailDTO getQnaDetail(Long qnaId) {
//        SellerQna qna = sellerQnaRepository.findByIdAndDeletedFalse(qnaId)
//                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));
//        return SellerQnaDetailDTO.fromEntity(qna);
//    }
//
//    /**
//     * 미답변 QnA 목록 조회 (isAnswered = false)
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<SellerQnaResponseDTO> getUnansweredQnaList(Long sellerId, Pageable pageable) {
//        Page<SellerQna> qnas = sellerQnaRepository.findBySellerIdAndIsAnsweredFalseAndDeletedFalse(sellerId, pageable);
//        return qnas.map(SellerQnaResponseDTO::fromEntity);
//    }
//
//    /**
//     * 답변 작성 및 수정
//     */
//    @Override
//    public void answerQnaByEmail(String email, Long qnaId, SellerQnaAnswerRequestDTO dto) {
//        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(email)
//                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
//        SellerQna qna = sellerQnaRepository.findByIdAndDeletedFalse(qnaId)
//                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));
//
//        // 본인의 QnA인지 확인
//        if (!qna.getSeller().getId().equals(seller.getId())) {
//            throw new SecurityException("해당 QnA에 답변 권한이 없습니다.");
//        }
//
//        qna.setAnswer(dto.getAnswer());
//        qna.setAnswered(true);
//        qna.setAnsweredAt(LocalDateTime.now());
//        sellerQnaRepository.save(qna);
//    }
//
//    /**
//     * QnA 삭제 (isActive = false 처리)
//     */
//    @Override
//    public void deleteQna(Long qnaId, String email) {
//        Seller seller = sellerRepository.findByEmailAndIsActiveTrue(email)
//                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
//        SellerQna qna = sellerQnaRepository.findByIdAndDeletedFalse(qnaId)
//                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));
//
//        if (!qna.getSeller().getId().equals(seller.getId())) {
//            throw new SecurityException("해당 QnA를 삭제할 권한이 없습니다.");
//        }
//
//        qna.setIsActive(false); // soft delete
//    }
//
//    /**
//     * 고객이 QnA 등록
//     */
//    @Override
//    public void createQna(SellerQnaRequestDTO dto) {
//        Seller seller = sellerRepository.findById(dto.getSellerId())
//                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
//        Customer customer = customerRepository.findById(dto.getCustomerId())
//                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
//
//        SellerQna qna = SellerQna.builder()
//                .seller(seller)
//                .customer(customer)
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .isAnswered(false)
//                .isActive(true)
//                .deleted(false)
//                .build();
//
//        sellerQnaRepository.save(qna);
//    }
//
//    /**
//     * 고객이 질문 내용 수정
//     */
//    @Override
//    public void updateQnaByCustomer(Long qnaId, String content, String title, Long customerId) {
//        SellerQna qna = sellerQnaRepository.findByIdAndDeletedFalse(qnaId)
//                .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));
//
//        if (!qna.getCustomer().getId().equals(customerId)) {
//            throw new SecurityException("자신이 작성한 QnA만 수정할 수 있습니다.");
//        }
//
//        qna.setTitle(title);
//        qna.setContent(content);
//    }
//}