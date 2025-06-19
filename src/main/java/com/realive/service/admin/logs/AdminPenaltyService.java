// AdminPenaltyService.java

package com.realive.service.admin.logs;

import com.realive.domain.logs.PenaltyLog;
import com.realive.dto.logs.PenaltyLogCreateRequest;
import com.realive.repository.logs.PenaltyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPenaltyService {
    private final PenaltyLogRepository penaltyLogRepository;

    public PenaltyLog create(PenaltyLogCreateRequest request) {
        PenaltyLog entity = new PenaltyLog();
        entity.setCustomerId(request.getCustomerId()); // getCustomerId()가 Long 타입이어야 함
        entity.setReason(request.getReason());
        entity.setPoints(request.getPoints());
        entity.setDescription(request.getDescription());
        return penaltyLogRepository.save(entity);
    }

    // 페이징 및 검색 메서드 (customerId 타입을 Long으로 변경)
    public Page<PenaltyLog> getPenalties(String reason, Long customerId, Pageable pageable) {
        if (reason != null && customerId != null) {
            return penaltyLogRepository.findByReasonContainingAndCustomerId(reason, customerId, pageable);
        } else if (reason != null) {
            return penaltyLogRepository.findByReasonContaining(reason, pageable);
        } else if (customerId != null) {
            return penaltyLogRepository.findByCustomerId(customerId, pageable);
        } else {
            return penaltyLogRepository.findAll(pageable);
        }
    }

    // penaltyId는 DB가 int4(Integer)이므로 그대로 유지
    public PenaltyLog getById(Integer penaltyId) {
        return penaltyLogRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("패널티를 찾을 수 없습니다."));
    }
}
