// AdminPenaltyController.java

package com.realive.controller.admin;

import com.realive.domain.customer.Customer;
import com.realive.domain.logs.PenaltyLog;
import com.realive.dto.logs.PenaltyLogCreateRequest;
import com.realive.dto.logs.PenaltyLogDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.logs.PenaltyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@RestController
@RequestMapping("/api/admin/penalties")
@RequiredArgsConstructor
public class AdminPenaltyController {

    private final PenaltyLogRepository penaltyLogRepository;
    private final CustomerRepository customerRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<PenaltyLogDTO> createPenalty(@RequestBody PenaltyLogCreateRequest request) {
        try {
            // 해당 고객 조회
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다. ID: " + request.getCustomerId()));

            // 패널티 로그 생성
            PenaltyLog entity = new PenaltyLog();
            entity.setCustomerId(request.getCustomerId());
            entity.setReason(request.getReason());
            entity.setPoints(request.getPoints());
            entity.setDescription(request.getDescription());
            PenaltyLog saved = penaltyLogRepository.save(entity);

            // 고객의 패널티 점수 증가
            int currentPenaltyScore = customer.getPenaltyScore() != null ? customer.getPenaltyScore() : 0;
            int newPenaltyScore = currentPenaltyScore + (request.getPoints() != null ? request.getPoints() : 0);
            
            customer.setPenaltyScore(newPenaltyScore);
            customerRepository.save(customer);

            log.info("패널티 등록 완료 - 패널티 ID: {}, 고객 ID: {}, 추가된 점수: {}, 새로운 총점: {}", 
                    saved.getId(), request.getCustomerId(), request.getPoints(), newPenaltyScore);

            return ResponseEntity.ok(PenaltyLogDTO.fromEntity(saved));
        } catch (EntityNotFoundException e) {
            log.error("패널티 등록 실패 - 고객을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("패널티 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 페이징+검색 (customerId 타입을 Long으로 변경)
    @GetMapping
    public ResponseEntity<Page<PenaltyLogDTO>> getPenalties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Long customerId // Integer → Long
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

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

    // 패널티 삭제 API 추가
    @DeleteMapping("/{penaltyId}")
    @Transactional
    public ResponseEntity<String> deletePenalty(@PathVariable("penaltyId") Integer penaltyId) {
        try {
            // 패널티 조회
            PenaltyLog penalty = penaltyLogRepository.findById(penaltyId)
                    .orElseThrow(() -> new EntityNotFoundException("패널티를 찾을 수 없습니다. ID: " + penaltyId));

            // 해당 고객 조회
            Customer customer = customerRepository.findById(penalty.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다. ID: " + penalty.getCustomerId()));

            // 패널티 점수 차감
            int currentPenaltyScore = customer.getPenaltyScore() != null ? customer.getPenaltyScore() : 0;
            int penaltyPoints = penalty.getPoints() != null ? penalty.getPoints() : 0;
            int newPenaltyScore = Math.max(0, currentPenaltyScore - penaltyPoints); // 음수가 되지 않도록
            
            customer.setPenaltyScore(newPenaltyScore);
            customerRepository.save(customer);

            // 패널티 삭제
            penaltyLogRepository.delete(penalty);

            log.info("패널티 삭제 완료 - 패널티 ID: {}, 고객 ID: {}, 차감된 점수: {}, 새로운 총점: {}", 
                    penaltyId, penalty.getCustomerId(), penaltyPoints, newPenaltyScore);

            return ResponseEntity.ok("패널티가 성공적으로 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            log.error("패널티 삭제 실패 - 엔티티를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("패널티 삭제 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("패널티 삭제 중 오류가 발생했습니다.");
        }
    }
}
