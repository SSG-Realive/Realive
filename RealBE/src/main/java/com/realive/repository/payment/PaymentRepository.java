package com.realive.repository.payment;

import com.realive.domain.payment.Payment;
import com.realive.domain.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    // === 관리자 대시보드용 집계 메서드들 ===
    
    /**
     * 특정 날짜에 완료된 결제 건수 조회
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE DATE(p.createdAt) = :date AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsByDate(@Param("date") LocalDate date);

    /**
     * 특정 기간에 완료된 결제 건수 조회
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜에 완료된 결제 총액 조회
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE DATE(p.createdAt) = :date AND p.status = 'COMPLETED'")
    Long sumCompletedPaymentAmountByDate(@Param("date") LocalDate date);

    /**
     * 특정 기간에 완료된 결제 총액 조회
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    Long sumCompletedPaymentAmountByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜/시간 범위에 완료된 결제 조회 (시간 포함)
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt BETWEEN :startDateTime AND :endDateTime AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsByDateTime(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 특정 날짜/시간 범위에 완료된 결제 총액 조회 (시간 포함)
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :startDateTime AND :endDateTime AND p.status = 'COMPLETED'")
    Long sumCompletedPaymentAmountByDateTime(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
