package cryss.dev.rabbit_basic.consumer;


import cryss.dev.rabbit_basic.event.ExcelCreatedEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ExcelConsumer {
    @RabbitListener
//    @RabbitListener(queues = "excel.v1.queue.file_imported")
//    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1_000, multiplier = 1.5, maxDelay = 10_000))
    public void onExcelFileImported(ExcelCreatedEvent event) {
        if (event.getPath () == null) {
            throw new IllegalArgumentException ("Path attribute can be a null value.");
        }
        log.info ("Excel cretead event={}", event);
    }
}
