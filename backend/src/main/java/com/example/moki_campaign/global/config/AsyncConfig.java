package com.example.moki_campaign.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 모키 API 호출용 비동기 Executor
     * - 코어 풀 사이즈: 5 (동시에 5개 API 호출)
     * - 최대 풀 사이즈: 10 (필요 시 확장)
     * - 큐 용량: 25 (대기 작업 최대 25개)
     */
    @Bean(name = "mokiApiExecutor")
    public Executor mokiApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 기본 스레드 풀 크기
        executor.setCorePoolSize(5);
        
        // 최대 스레드 풀 크기
        executor.setMaxPoolSize(10);
        
        // 큐 용량
        executor.setQueueCapacity(25);
        
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("MokiAPI-");
        
        // 거부 정책: 호출한 스레드에서 직접 실행
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 애플리케이션 종료 시 대기 중인 작업 완료
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 종료 대기 시간 (초)
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("MokiAPI Executor 초기화 완료 - CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
