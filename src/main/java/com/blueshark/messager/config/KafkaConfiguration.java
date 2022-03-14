package com.blueshark.messager.config;


import com.blueshark.messager.deserializer.EmailTemplateDeserializer;
import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.serializer.EmailTemplateSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConfiguration {

    final Logger logger = LoggerFactory.getLogger(KafkaConfiguration.class);
    @Value("${configuration.kafka.bootstrapServer}")
    private String bootstrapServer;
    @Value("${configuration.kafka.topic}")
    private String topic;
    @Value("${configuration.kafka.group}")
    private String group;
    @Value("${configuration.kafka.clientId}")
    private String clientId;

    private KafkaProperties kafkaProperties;

    @Bean
    public ReceiverOptions<Integer, EmailTemplate> receiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EmailTemplateDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 50000);

        ReceiverOptions<Integer, EmailTemplate> receiverOptions = ReceiverOptions.create(props);

        return receiverOptions.subscription(Collections.singleton(topic))
                .addAssignListener(partitions -> logger.debug("onPartitionsAssigned : " + partitions))
                .addRevokeListener(partitions -> logger.debug("onPartitionsRevoked : " + partitions));
    }

    @Bean
    public KafkaSender<Integer, EmailTemplate> producerOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EmailTemplateSerializer.class);
        SenderOptions<Integer, EmailTemplate> senderOptions = SenderOptions.create(props);

        return KafkaSender.create(senderOptions);
    }
}
