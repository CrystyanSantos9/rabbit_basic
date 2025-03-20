package cryss.dev.rabbit_basic;

import cryss.dev.rabbit_basic.config.rabbit.RabbitConfigBase;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class RabbitBasicApplication implements CommandLineRunner {

	@Autowired
	private RabbitTemplate template;

	public static void main(String[] args) {
		SpringApplication.run(RabbitBasicApplication.class, args);
	}

	@Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
	private void publicMessage(){
		Message msgRabbitInBytes = new Message ("algo".getBytes ());
		template.send (RabbitConfigBase.EXCEL_FILE_IMPORTED, msgRabbitInBytes);
	}
	@Override
	public void run(String... args) throws Exception {
		publicMessage ();
	}
}
