package com.realive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    // WebClient 내부에서만 사용할 ObjectMapper를 만드는 private 헬퍼 메소드
    private ObjectMapper createWebClientObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // WebClient만을 위한 특별한 설정이 있다면 여기에 추가합니다.
        return mapper;
    }

    // 두 개의 webClient() 메소드를 하나로 합친 최종 Bean
    @Bean
    public WebClient webClient() {
        // 1. HttpClient 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));

        // 2. ObjectMapper 및 ExchangeStrategies 설정
        ObjectMapper customMapper = createWebClientObjectMapper();
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> 
                        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(customMapper)))
                .build();

        // 3. 위 설정들을 모두 사용하여 WebClient를 생성하고 반환
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .baseUrl("https://api.tosspayments.com")
                .build();
    }
}