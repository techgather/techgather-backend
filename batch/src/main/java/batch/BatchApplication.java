package batch;

import application.notification.DiscordNotificationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = {"batch", "domain"})
@EntityScan(basePackages = {"domain.entity"})
@EnableJpaRepositories(basePackages = {"domain.repository"})
@EnableScheduling
@EnableAsync
@Import(DiscordNotificationConfig.class)
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
}
