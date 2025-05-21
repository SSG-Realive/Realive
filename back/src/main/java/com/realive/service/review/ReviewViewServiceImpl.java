package com.realive.service.review;

import com.realive.dto.review.ReviewResponseDTO;
import com.realive.exception.NotFoundException;
import com.realive.repository.review.ReviewViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class ReviewViewServiceImpl implements ReviewViewService {

    private final ReviewViewRepository reviewViewRepository;

    @Override
    public Page<ReviewResponseDTO> getReviewList(Long sellerId, Pageable pageable) {
        log.info("Fetching review list for sellerId: {}, page: {}", sellerId, pageable.getPageNumber());
        Page<ReviewResponseDTO> reviews = reviewViewRepository.findSellerReviewsBySellerId(sellerId, pageable);
        log.info("Retrieved {} reviews for sellerId: {}", reviews.getTotalElements(), sellerId);
        return reviews;
    }

    @Override
    public ReviewResponseDTO getReviewDetail(Long id) {
        log.info("Fetching review detail for id: {}", id);
        ReviewResponseDTO review = reviewViewRepository.findReviewDetailById(id)
                .orElseThrow(() -> {
                    log.error("Review not found for id: {}", id);
                    return new NotFoundException("해당 리뷰가 존재하지 않습니다. id=" + id);
                });
        log.info("Retrieved review detail for id: {}", id);
        return review;
    }
}
