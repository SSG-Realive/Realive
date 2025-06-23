// AdminPenaltyController.java

package com.realive.controller.admin;

import com.realive.domain.logs.PenaltyLog;
import com.realive.dto.logs.PenaltyLogCreateRequest;
import com.realive.dto.logs.PenaltyLogDTO;
import com.realive.repository.logs.PenaltyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/penalties")
@RequiredArgsConstructor
public class AdminPenaltyController {

    private final PenaltyLogRepository penaltyLogRepository;

    @PostMapping
    public ResponseEntity<PenaltyLogDTO> createPenalty(@RequestBody PenaltyLogCreateRequest request) {
        PenaltyLog entity = new PenaltyLog();
        entity.setCustomerId(request.getCustomerId());
        entity.setReason(request.getReason());
        entity.setPoints(request.getPoints());
        entity.setDescription(request.getDescription());
        PenaltyLog saved = penaltyLogRepository.save(entity);
        return ResponseEntity.ok(PenaltyLogDTO.fromEntity(saved));
    }

    // 페이징+검색 (customerId 타입을 Long으로 변경)
    @GetMapping
    public ResponseEntity<Page<PenaltyLogDTO>> getPenalties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Long customerId // Integer → Long
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PenaltyLog> penalties;
        if (reason != null && customerId != null) {
            penalties = penaltyLogRepository.findByReasonContainingAndCustomerId(reason, customerId, pageable);
        } else if (reason != null) {
            penalties = penaltyLogRepository.findByReasonContaining(reason, pageable);
        } else if (customerId != null) {
            penalties = penaltyLogRepository.findByCustomerId(customerId, pageable);
        } else {
            penalties = penaltyLogRepository.findAll(pageable);
        }

        Page<PenaltyLogDTO> dtos = penalties.map(PenaltyLogDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    // penaltyId는 DB가 int4(Integer)이므로 그대로 유지
    @GetMapping("/{penaltyId:[0-9]+}")
    public ResponseEntity<PenaltyLogDTO> getPenalty(@PathVariable("penaltyId") Integer penaltyId) {
        PenaltyLog penalty = penaltyLogRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("패널티를 찾을 수 없습니다."));
        return ResponseEntity.ok(PenaltyLogDTO.fromEntity(penalty));
    }
}
