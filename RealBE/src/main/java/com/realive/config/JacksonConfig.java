package com.realive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // JavaTimeModule을 등록하여 LocalDate, LocalDateTime 등을 지원하게 합니다.
        // 이 모듈이 "yyyy-MM-dd" 형식의 문자열을 LocalDate로,
        // "yyyy-MM-dd'T'HH:mm:ss" 형식의 문자열을 LocalDateTime으로 변환해줍니다.
        mapper.registerModule(new JavaTimeModule());

        // 날짜/시간을 timestamp(숫자)가 아닌, ISO-8601 표준 문자열 형식으로 직렬화하도록 설정합니다.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}