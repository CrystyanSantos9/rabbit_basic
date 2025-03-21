package cryss.dev.rabbit_basic.consumer;


import cryss.dev.rabbit_basic.event.ExcelCreatedEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ExcelConsumer {
//    @RabbitListener(queues = "excel.v1.queue.file_imported", containerFactory = "rabbitListenerContainerFactory")
    @RabbitListener(queues = "excel.v1.queue.file_imported")
    public void onExcelFileImported(ExcelCreatedEvent event){
        log.info ("Excel cretead event={}", event);
    }
}
