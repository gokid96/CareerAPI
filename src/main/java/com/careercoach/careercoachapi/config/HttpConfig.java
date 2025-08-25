package com.careercoach.careercoachapi.config;

// 필요한 의존성 import
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

@Configuration  // 스프링 설정 클래스임을 나타냄
public class HttpConfig {

    @Bean   // 스프링 컨테이너에 빈으로 등록
    public WebClient webClient() {
        // ConnectionProvider 설정: HTTP 연결 풀 관리를 위한 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("career-coach-pool")
                .maxConnections(20)                              // 동시에 유지할 수 있는 최대 연결 수
                .maxIdleTime(Duration.ofSeconds(20))            // 유휴 상태의 연결을 유지할 최대 시간
                .maxLifeTime(Duration.ofSeconds(60))            // 연결의 최대 수명 시간
                .pendingAcquireTimeout(Duration.ofSeconds(5))   // 연결 획득 대기 제한 시간
                .evictInBackground(Duration.ofSeconds(30))      // 백그라운드에서 만료된 연결 제거 주기
                .build();

        // HttpClient 설정: 기본적인 HTTP 클라이언트 동작 설정
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // 초기 연결 타임아웃 설정
                .option(ChannelOption.SO_KEEPALIVE, true)           // TCP keepalive 활성화
                .responseTimeout(Duration.ofSeconds(30))            // 응답 대기 제한 시간
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))    // 읽기 타임아웃 설정
                                .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)) // 쓰기 타임아웃 설정
                );

        // WebClient 빌더를 사용하여 최종 WebClient 인스턴스 생성
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))  // 설정된 HttpClient 연결
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 최대 메모리 버퍼 크기를 2MB로 설정
                .build();
    }
}