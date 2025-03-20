package cryss.dev.rabbit_basic.config.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigBase {

    @Autowired RabbitProperties rabbitProperties;
        public static final String FANAUT_EXCHANGE_NAME = "excel.exchange.fanout.events";
        public static final String EXCEL_FILE_IMPORTED = "excel.v1.queue.file_imported";

    @Bean
    public Queue fileImportedQueue() {
        return new Queue (EXCEL_FILE_IMPORTED);
    }


    @Bean
    CachingConnectionFactory getCachedConnection(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory (rabbitProperties.getHost ());
        connectionFactory.setUsername(rabbitProperties.getUsername ());
        connectionFactory.setPassword(rabbitProperties.getPassword ());
        connectionFactory.setConnectionNameStrategy(connection -> "MY_CONNECTION");
        return connectionFactory;
    }


    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin (connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(RabbitAdmin rabbitAdmin){
        return event -> rabbitAdmin.initialize ();
    }

    //SERIALIZAR MENSAGENS


    //LISTERNER CONTAINER https://docs.spring.io/spring-amqp/reference/amqp/receiving-messages/using-container-factories.html

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(getCachedConnection());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setContainerCustomizer(container ->
                container.addQueues (fileImportedQueue())
        );
        return factory;
    }


}
