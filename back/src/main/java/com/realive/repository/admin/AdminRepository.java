package com.realive.repository.admin;

import com.realive.domain.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByEmail(String email);

//    /**
//     * 관리자 이름으로 관리자 찾기
//     *
//     * @param name 관리자 이름
//     * @return 관리자 목록
//     */
//    List<Admin> findByName(String name);
//
//    /**
//     * 이메일로 관리자 찾기
//     *
//     * @param email 관리자 이메일
//     * @return 관리자 (없을 수 있음)
//     */
//
//
//    /**
//     * 관리자 역할로 관리자 찾기
//     *
//     * @param role 관리자 역할
//     * @return 관리자 목록
//     */
//    List<Admin> findByRole(String role);
//
//    /**
//     * 활성 상태인 관리자 목록 찾기
//     *
//     * @return 활성 상태인 관리자 목록
//     */
//    List<Admin> findByActiveTrue();
//
//    /**
//     * 특정 기간에 생성된 관리자 목록 찾기
//     *
//     * @param startDate 시작 날짜
//     * @param endDate 종료 날짜
//     * @return 해당 기간에 생성된 관리자 목록
//     */
//    @Query("SELECT a FROM Admin a WHERE a.createdAt BETWEEN :startDate AND :endDate")
//    List<Admin> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
//                                       @Param("endDate") LocalDateTime endDate);
//
//    /**
//     * 특정 역할과 활성 상태를 가진 관리자 수 카운트
//     *
//     * @param role 관리자 역할
//     * @param active 활성 상태
//     * @return 조건에 맞는 관리자 수
//     */
//    long countByRoleAndActive(String role, boolean active);
//
//    /**
//     * 로그인 ID로 관리자 찾기
//     *
//     * @param loginId 로그인 ID
//     * @return 관리자 (없을 수 있음)
//     */
//    Optional<Admin> findByLoginId(String loginId);
//
//    /**
//     * 판매자 승인 처리를 담당한 관리자 찾기
//     *
//     * @param sellerId 판매자 ID
//     * @return 해당 판매자를 승인한 관리자 목록
//     */
////    @Query("SELECT a FROM Admin a JOIN SellerApproval sa ON a.id = sa.admin.id WHERE sa.seller.id = :sellerId")
////    List<Admin> findAdminsByApprovedSellerId(@Param("sellerId") Integer sellerId);
//
//    /**
//     * 특정 기간 동안 처리한 판매자 승인 건수로 관리자 정렬
//     *
//     * @param startDate 시작 날짜
//     * @param endDate 종료 날짜
//     * @return 승인 처리 건수와 관리자 정보
//     */
////    @Query("SELECT a, COUNT(sa) FROM Admin a JOIN SellerApproval sa ON a.id = sa.admin.id " +
////            "WHERE sa.approvedAt BETWEEN :startDate AND :endDate " +
////            "GROUP BY a.id ORDER BY COUNT(sa) DESC")
////    List<Object[]> findAdminsByApprovalCountBetween(@Param("startDate") LocalDateTime startDate,
////                                                    @Param("endDate") LocalDateTime endDate);
}