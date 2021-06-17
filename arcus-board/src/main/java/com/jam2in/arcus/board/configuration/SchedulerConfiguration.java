package com.jam2in.arcus.board.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {
	private static final int THREAD_POOL_SIZE = 4;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

		threadPoolTaskScheduler.setPoolSize(THREAD_POOL_SIZE);
		threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-pool-");
		threadPoolTaskScheduler.initialize();

		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
	}
}
