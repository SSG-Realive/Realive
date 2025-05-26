package com.realive.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// QueryDSL 설정 클래스
// JPAQueryFactory를 빈으로 등록하여 프로젝트 전반에서 DI로 사용 가능하게 함

@Configuration
@RequiredArgsConstructor
public class QuerydslConfig {

    // JPA의 EntityManager를 주입 받음 (QueryDSL 내부에서 사용됨)
    private final EntityManager em;


    // JPAQueryFactory를 Spring Bean으로 등록
    // 이 Bean을 통해 Repository나 Service 계층에서 QueryDSL 사용 가능
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}