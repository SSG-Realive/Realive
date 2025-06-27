package com.realive.controller.admin.ranking;

import com.realive.dto.admin.review.SellerRankingDTO;
import com.realive.service.admin.logs.StatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자용 판매자 평점 랭킹 조회 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/admin/sellers")
public class AdminSellerRankingController {

    private final StatService statService;

    public AdminSellerRankingController(StatService statService) {
        this.statService = statService;
    }

    /**
     * 판매자별 평점 랭킹을 조회합니다.
     * 리뷰 수가 minReviews 미만인 판매자는 제외합니다.
     * 결과는 avgRating 내림차순, reviewCount 내림차순으로 정렬됩니다.
     *
     * @param minReviews 최소 리뷰 수 (기본값 = 3)
     * @param pageable   페이징 및 정렬 정보 (기본: size=20, sort=avgRating DESC)
     * @return 페이징된 판매자 랭킹 DTO
     */
    @GetMapping("/ranking")
    public Page<SellerRankingDTO> getSellerRanking(
            @RequestParam(name = "minReviews", defaultValue = "1") long minReviews,
            @PageableDefault(size = 20, sort = "avgRating", direction = Direction.DESC) Pageable pageable
    ) {
        return statService.getRanking(minReviews, pageable);
    }
}
