package cryss.dev.rabbit_basic.config.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class RabbitConfigBase {

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired RabbitProperties rabbitProperties;
        public static final String EXCEL_FANOUT_EXCHANGE_NAME = "excel.exchange.fanout.events";
        public static final String EXCEL_FILE_IMPORTED = "excel.v1.queue.file_imported";


        @Bean
        public FanoutExchange fanoutExchange(){
            return new FanoutExchange (EXCEL_FANOUT_EXCHANGE_NAME);
        }

//    @Bean
//    public Queue fileImportedQueue() {
//        return new Queue (EXCEL_FILE_IMPORTED);
//    }


    @Bean
    @Primary
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
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(new JavaTimeModule ());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate (connectionFactory);
        rabbitTemplate.setMessageConverter (messageConverter);

        return rabbitTemplate;
    }

    //LISTERNER CONTAINER https://docs.spring.io/spring-amqp/reference/amqp/receiving-messages/using-container-factories.html

//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(getCachedConnection());
//        factory.setConcurrentConsumers(3);
////        factory.setMessageConverter (messageConverter());
//        factory.setMaxConcurrentConsumers(10);
//        factory.setContainerCustomizer(container ->
//                container.addQueues (fileImportedQueue())
//        );
//        return factory;
//    }


}
