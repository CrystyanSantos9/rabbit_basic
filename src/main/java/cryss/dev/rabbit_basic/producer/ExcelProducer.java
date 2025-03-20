package cryss.dev.rabbit_basic.producer;

import cryss.dev.rabbit_basic.event.ExcelCreatedEvent;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cryss.dev.rabbit_basic.config.rabbit.RabbitConfigBase.EXCEL_FILE_IMPORTED;

@Component
public class ExcelProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void excelCreatedPublish(ExcelCreatedEvent event){
        rabbitTemplate.convertAndSend (EXCEL_FILE_IMPORTED, event);
    }
}
