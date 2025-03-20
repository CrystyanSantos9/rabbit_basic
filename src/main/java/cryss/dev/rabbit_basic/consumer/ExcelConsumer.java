package cryss.dev.rabbit_basic.consumer;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ExcelConsumer {
//    @RabbitListener(queues = "excel.v1.queue.file_imported", containerFactory = "rabbitListenerContainerFactory")
////    @RabbitListener(queues = RabbitConfigBase.EXCEL_FILE_IMPORTED, containerFactory = "rabbitListenerContainerFactory")
//    public void onExcelFileImported(Message event){
//        System.out.println ("Olha a mensagem " + new String (event.getBody ()));
//    }
}
