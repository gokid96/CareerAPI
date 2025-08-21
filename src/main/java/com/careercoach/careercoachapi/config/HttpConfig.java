package com.careercoach.careercoachapi.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpConfig {

    @Bean
    public WebClient webClient() {
        // 연결 풀 설정 (성능 핵심!)
        ConnectionProvider connectionProvider = ConnectionProvider.builder("career-coach-pool")
                .maxConnections(20)          // 최대 연결 수
                .maxIdleTime(Duration.ofSeconds(20))  // 유휴 연결 유지 시간
                .maxLifeTime(Duration.ofSeconds(60))  // 연결 최대 생존 시간
                .pendingAcquireTimeout(Duration.ofSeconds(5))  // 연결 대기 시간
                .evictInBackground(Duration.ofSeconds(30))     // 백그라운드 정리
                .build();

        // HTTP 클라이언트 설정
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // 연결 타임아웃
                .option(ChannelOption.SO_KEEPALIVE, true)            // Keep-Alive 활성화
                .responseTimeout(Duration.ofSeconds(30))              // 응답 타임아웃
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB 버퍼
                .build();
    }
}