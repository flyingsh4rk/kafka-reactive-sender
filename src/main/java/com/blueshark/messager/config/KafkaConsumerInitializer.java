package com.blueshark.messager.config;

import com.blueshark.messager.exceptions.SendingException;
import com.blueshark.messager.handler.CallbackHandler;
import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.MessageType;
import com.blueshark.messager.model.SendingError;
import com.blueshark.messager.sender.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KafkaConsumerInitializer {

    final Logger logger = LoggerFactory.getLogger(KafkaConsumerInitializer.class);
    final private Integer consumerNumber;
    final private Integer backOff;
    final private ReceiverOptions<Integer, EmailTemplate> receiverOptions;
    final private Map<MessageType, Sender> senderMap;
    final private CallbackHandler callbackHandler;

    public KafkaConsumerInitializer(@Value("${configuration.consumer-number}") Integer consumerNumber
            , @Value("${configuration.consumer-backoff}") Integer backOff
            , ReceiverOptions<Integer, EmailTemplate> receiverOptions
            , List<Sender> senders, CallbackHandler callbackHandler) {
        this.consumerNumber = consumerNumber;
        this.receiverOptions = receiverOptions;
        this.senderMap = senders.stream().collect(Collectors.toMap(Sender::getAcceptType, Function.identity()));
        this.backOff = backOff;
        this.callbackHandler = callbackHandler;
    }

    public void kafkaConsumerInit() {
        if (consumerNumber < 1) {
            throw new IllegalArgumentException("Consumer number can't be less than 1!");
        }

        for (int i = 0; i < consumerNumber; i++) {
            Flux<ReceiverRecord<Integer, EmailTemplate>> kafkaReceiver = Flux.defer(() -> {
                logger.debug("Creating new consumer");
                return KafkaReceiver.create(receiverOptions).receive();
            });
            kafkaReceiver.subscribeOn(Schedulers.single()).flatMap(event -> {
                        EmailTemplate message = event.value();
                        MessageType type = message.getType();
                        logger.info(String.format("Receive message template: %s", message));
                        Sender sender = senderMap.get(type);
                        if (sender == null) {
                            event.receiverOffset().commit();
                            return Mono.error(new SendingException(List.of(
                                    new SendingError(new HashSet<>(message.getReceivers())
                                            , new IllegalStateException("Message Type is not supported"))), message));
                        }
                        return Mono.fromCallable(() -> sender.send(message)).flatMap(result -> {
                                    if (result.size() == 0) {
                                        return Mono.zip(Mono.just(message), Mono.just(result));
                                    }
                                    return Mono.error(new SendingException(result, message));
                                }).subscribeOn(Schedulers.boundedElastic())
                                .onErrorResume(SendingException.class, (e) -> {
                                    // Fail handler
                                    return callbackHandler.callback(e.getTemplate().getApplicationId(), e.getTemplate().getMessageId(), e.getErrors())
                                            .doOnError(callbackError -> logger.error("Sending callback error: ", callbackError))
                                            .onErrorResume((callbackException) -> Mono.empty())
                                            .flatMap((dummy) -> Mono.empty());

                                })
                                .flatMap(TupleUtils.function((successMessage, result) -> {
                                    // Success handler
                                    return callbackHandler.callback(successMessage.getApplicationId(), successMessage.getMessageId(), null);
                                }))
                                .thenEmpty(event.receiverOffset().commit());
                    })
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                    .doOnError(e -> {
                        // TODO: notify when consumer is down here
                        logger.error("Consumer is down : ", e);
                    })
                    .subscribe();
        }
    }
}
