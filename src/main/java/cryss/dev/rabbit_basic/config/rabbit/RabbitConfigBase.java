package cryss.dev.rabbit_basic.config.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
@Log4j2
public class RabbitConfigBase {

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired RabbitProperties rabbitProperties;
        public static final String EXCEL_FANOUT_EXCHANGE_NAME = "excel.exchange.fanout.events";
        public static final String EXCEL_FILE_IMPORTED = "excel.v1.queue.file_imported";


    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange (EXCEL_FANOUT_EXCHANGE_NAME);
    }

    @Bean
    public Queue fileImportedQueue() {
        return new Queue (EXCEL_FILE_IMPORTED);
    }

    @Bean
    public Binding bindWithExcelCreatedEvent(){
        return BindingBuilder.bind (fileImportedQueue ()).to (fanoutExchange ());
    }

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
    CachingConnectionFactory getSimpleListenerCachedConnection() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory (rabbitProperties.getHost ());
        connectionFactory.setUsername (rabbitProperties.getUsername ());
        connectionFactory.setPassword (rabbitProperties.getPassword ());
        connectionFactory.setConnectionNameStrategy (connection -> "LISTENER_CONNECTION");
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
    RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate (getCachedConnection());
        rabbitTemplate.setMessageConverter (messageConverter());

        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);


        return rabbitTemplate;
    }

    //LISTERNER CONTAINER https://docs.spring.io/spring-amqp/reference/amqp/receiving-messages/using-container-factories.html

    @Bean
    public RetryTemplate simpleListenerRetryTemplate(){

        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy ();
        backOffPolicy.setInitialInterval (5000);
        backOffPolicy.multiplierSupplier (() -> 2.0D);
        backOffPolicy.setMaxInterval (10000);


        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        retryTemplate.setBackOffPolicy (backOffPolicy);
        retryTemplate.setRetryPolicy (retryPolicy);


//        return RetryTemplate.builder ()
//                .exponentialBackoff (5000, 2D, 10000)
//                .build ();
////                .customPolicy (retryPolicy)
////                .customBackoff (backOffPolicy).build ();
        return retryTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(getSimpleListenerCachedConnection());
        factory.setConcurrentConsumers(3);
        factory.setMessageConverter (messageConverter());
        factory.setMaxConcurrentConsumers(10);
//        factory.setContainerCustomizer(container ->
//                container.addQueues (fileImportedQueue())
//        );
        factory.setRetryTemplate (simpleListenerRetryTemplate());
        factory.setDefaultRequeueRejected (Boolean.FALSE);

        return factory;
    }

    @Bean
    public SimpleMessageListenerContainer factoryCreatedContainerSimpleListener() {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setQueueNames("excel.v1.queue.file_imported");
        endpoint.setMessageListener(message -> {
            log.info (message.getMessageProperties ().toString ());
            log.info (new String (message.getBody ()));
            throw new IllegalArgumentException ("Path attribute can be a null value.");
        });
        return rabbitListenerContainerFactory().createListenerContainer(endpoint);
    }


    int counter =0;

    private void verifyNwConfiguration(){
        counter++;
        log.info("N/W configuration Service Failed "+ counter);
        throw new RuntimeException();
    }



}
