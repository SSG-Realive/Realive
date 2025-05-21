package com.realive.repository.logs;

import com.realive.domain.logs.PenaltyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PenaltyLogRepository extends JpaRepository<PenaltyLog, Integer> {

    Optional<PenaltyLog> findById(Integer id);

    @Query("select pl.id, pl.customerId, pl.reason, pl.description " +
            " from PenaltyLog pl ")
    Page<Object[]> list1(Pageable pageable);
}
