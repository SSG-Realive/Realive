package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSummaryStatsDTO {
    private long totalMembers;           // 전체 회원 수 (누적)
    private long activeMembers;
    private long inactiveMembers;
    private long totalSellers;
    private long activeSellers;
    private long inactiveSellers;
    private long newMembersInPeriod;     // 기간 내 신규 가입자 수
    private long uniqueVisitorsInPeriod; // 기간 내 고유 방문자 수 (예: 사이트/앱에 접속한 순수 방문자)
    private long engagedUsersInPeriod;   // 기간 내 의미 있는 행동을 한 사용자 수 (예: 상품 조회, 로그인 시도 등)
    private long activeUsersInPeriod;    // 기간 내 활동 사용자 수 (이 필드의 구체적인 정의는 unique 또는 engaged와 다를 수도, 혹은 둘 중 하나를 대표할 수도 있음)
}
