package com.realive.repository.logs;

import com.realive.domain.logs.PenaltyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PenaltyLogRepository extends JpaRepository<PenaltyLog, Integer> {

    Optional<PenaltyLog> findById(Integer id);

    @Query("select pl.id, pl.customerId, pl.reason, pl.description " +
            " from PenaltyLog pl ")
    Page<Object[]> list1(Pageable pageable);

    // 특정 날짜에 생성된 패널티 건수
    @Query("SELECT COUNT(pl) FROM PenaltyLog pl WHERE DATE(pl.createdAt) = :date")
    Integer countByCreatedAtDate(LocalDate date);

    // 특정 기간 내 패널티 포인트 합계
    @Query("SELECT SUM(pl.points) FROM PenaltyLog pl WHERE pl.createdAt BETWEEN :startDateTime AND :endDateTime")
    Integer sumPointsByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<PenaltyLog> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

}
