package com.jam2in.arcus.board.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {
	private static final int TASK_CORE_POOL_SIZE = 1;
	private static final int TASK_MAX_POOL_SIZE = 10;

	@Bean(name = "CachingExecutor")
	public Executor cachingExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(TASK_CORE_POOL_SIZE);
		threadPoolTaskExecutor.setThreadNamePrefix("caching-task-pool-");
		threadPoolTaskExecutor.initialize();
		return threadPoolTaskExecutor;
	}

	@Bean(name = "PutExecutor")
	public Executor putExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(TASK_CORE_POOL_SIZE);
		threadPoolTaskExecutor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
		threadPoolTaskExecutor.setThreadNamePrefix("put-task-pool-");
		threadPoolTaskExecutor.initialize();
		return threadPoolTaskExecutor;
	}
}
