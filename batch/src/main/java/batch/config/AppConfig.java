package batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AppConfig {

	private static final String THREAD_NAME_PREFIX = "tech-gather-";

	@Bean(name = "taskScheduler")
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
		scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
		scheduler.initialize();
		return scheduler;
	}
}
