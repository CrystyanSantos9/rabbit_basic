package cryss.dev.rabbit_basic;

import cryss.dev.rabbit_basic.config.rabbit.RabbitConfigBase;
import cryss.dev.rabbit_basic.consumer.ExcelConsumer;
import cryss.dev.rabbit_basic.event.ExcelCreatedEvent;
import cryss.dev.rabbit_basic.producer.ExcelProducer;
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
	private ExcelProducer producer;

	public static void main(String[] args) {
		SpringApplication.run(RabbitBasicApplication.class, args);
	}

	@Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
	private void publicMessage(){
		ExcelCreatedEvent event = new ExcelCreatedEvent ("1");
		producer.excelCreatedPublish (event);

	}
	@Override
	public void run(String... args) throws Exception {
		publicMessage ();
	}
}
