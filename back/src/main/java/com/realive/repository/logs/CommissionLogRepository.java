package com.realive.repository.logs;

import com.realive.domain.logs.CommissionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommissionLogRepository extends JpaRepository<CommissionLog, Integer> {

    Optional<CommissionLog> findById(Integer id);

    @Query("select cl.id, cl.salesLogId, cl.commissionRate, cl.commissionAmount, " +
            "  cl.recordedAt " +
            " from CommissionLog cl ")
    Page<Object[]> list1(Pageable pageable);
}
