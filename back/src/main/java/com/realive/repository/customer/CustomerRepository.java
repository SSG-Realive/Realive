package com.realive.repository.customer;

package com.realive.repository.customer;

import com.realive.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// import java.math.BigDecimal; // 통계에 필요시
// import java.time.LocalDateTime; // 통계에 필요시

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * 특정 고객 ID로 고객 정보를 조회합니다. (기본 제공 findById와 동일하나 명시적 선언)
     * @param customerId 고객 ID
     * @return Customer 엔티티 (Optional은 JpaRepository 기본 제공)
     */
    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Customer findCustomerById(@Param("customerId") Long customerId); // Optional<Customer>로 반환하는 것이 더 안전


    /**
     * 이름에 특정 키워드가 포함된 고객 목록을 페이징하여 조회합니다. (대소문자 무시)
     * @param nameKeyword 이름 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 고객 목록 페이지
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :nameKeyword, '%'))")
    Page<Customer> findByNameContainingIgnoreCase(@Param("nameKeyword") String nameKeyword, Pageable pageable);

    /**
     * 이메일에 특정 키워드가 포함된 고객 목록을 페이징하여 조회합니다. (대소문자 무시)
     * @param emailKeyword 이메일 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 고객 목록 페이지
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.email) LIKE LOWER(CONCAT('%', :emailKeyword, '%'))")
    Page<Customer> findByEmailContainingIgnoreCase(@Param("emailKeyword") String emailKeyword, Pageable pageable);

    /**
     * 이름 또는 이메일에 특정 키워드가 포함된 고객 목록을 페이징하여 조회합니다. (대소문자 무시)
     * CustomerManagementServiceImpl에서 사용.
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 고객 목록 페이지
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Customer> findByNameOrEmailContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);


    /**
     * 활성화된 고객 수를 조회합니다.
     * Customer 엔티티에 'isActive' 필드가 boolean 타입으로 있다고 가정합니다.
     * @return 활성 고객 수
     */
    @Query("SELECT COUNT(c.id) FROM Customer c WHERE c.isActive = true")
    long countActiveCustomers();


    // --- 어드민 대시보드용: 신규 가입 고객 (생성일 기준 최신순) ---
    /**
     * 신규 가입 고객 목록을 페이징하여 최신순으로 조회합니다.
     * Customer 엔티티의 생성일 필드명(예: created)을 기준으로 정렬합니다.
     * @param pageable 페이징 정보 (Sort.by(Sort.Direction.DESC, "created") 포함)
     * @return 신규 고객 목록 페이지
     */
    // JpaRepository의 findAll(Pageable pageable)를 사용하고 서비스단에서 Sort 객체 전달 권장.
    // 명시적으로 JPQL로 작성한다면:
    // @Query("SELECT c FROM Customer c ORDER BY c.created DESC")
    // Page<Customer> findNewCustomers(Pageable pageable);


    // --- 고객 통계 관련 (주로 OrderRepository와 연동하여 서비스단에서 처리) ---
    // 아래 메소드들은 서비스의 복잡도를 낮추기 위해 리포지토리에 둘 수 있으나,
    // 일반적으로 OrderRepository에서 고객 ID를 기준으로 집계하는 것이 더 적절할 수 있습니다.

    /**
     * (예시) 특정 고객의 총 주문 건수를 조회합니다. (OrderRepository에 구현 권장)
     * @param customerId 고객 ID
     * @return 총 주문 건수
     */
    // @Query("SELECT COUNT(o.id) FROM Order o WHERE o.customer.id = :customerId")
    // Long countTotalOrdersForCustomer(@Param("customerId") Long customerId);

    /**
     * (예시) 특정 고객의 총 구매 금액을 조회합니다. (OrderRepository에 구현 권장)
     * @param customerId 고객 ID
     * @return 총 구매 금액
     */
    // @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.customer.id = :customerId")
    // BigDecimal sumTotalSpentForCustomer(@Param("customerId") Long customerId);

    // JpaSpecificationExecutor를 상속받았으므로, CustomerManagementServiceImpl의 searchCustomers에서
    // 더 복잡한 동적 검색은 Specification API를 활용할 수 있습니다.
    // (이미 Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase 로 대체함)
}
