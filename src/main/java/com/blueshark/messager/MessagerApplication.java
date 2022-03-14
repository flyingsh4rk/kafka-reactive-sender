package com.blueshark.messager;

import com.blueshark.messager.config.KafkaConsumerInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {
        "com.blueshark"
})
public class MessagerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MessagerApplication.class, args);
        KafkaConsumerInitializer consumerInitializer = applicationContext.getBean(KafkaConsumerInitializer.class);
        consumerInitializer.kafkaConsumerInit();
    }

}
