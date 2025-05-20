package com.realive.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.review.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Integer>{

    @Query("select rr from ReviewReport rr where rr.id = :id ")
    ReviewReport selectOne(@Param("id") Integer id);



    @Query("select rr.id, rr.sellerReviewId, rr.reporterId, rr.reason, rr.createdAt " +
            " from ReviewReport rr " + " order by rr.createdAt desc " )
    Page<Object[]> list1(Pageable pageable);
} 