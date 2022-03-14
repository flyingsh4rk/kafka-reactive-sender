package com.blueshark.messager.handler;

import com.blueshark.messager.config.ApplicationCallbackMapping;
import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.MessageResponse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

@Component
public class EmailHandler {

    private final KafkaSender<Integer, EmailTemplate> sender;

    private final ApplicationCallbackMapping applicationCallbackMapping;

    private final Validator validator;

    @Value("${configuration.kafka.topic}")
    private String topic;

    public EmailHandler(KafkaSender<Integer, EmailTemplate> sender, ApplicationCallbackMapping applicationCallbackMapping, Validator validator) {
        this.sender = sender;
        this.applicationCallbackMapping = applicationCallbackMapping;
        this.validator = validator;
    }

    private Mono<ServerResponse> onValidationErrors(
            Errors errors,
            EmailTemplate emailTemplate,
            ServerRequest request) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errors.getAllErrors().toString());
    }

//    public Mono<ServerResponse> send(ServerRequest request) {
//        Mono<ServerResponse> serverResponseMono = request.bodyToMono(EmailTemplate.class)
//                .flatMap(emailTemplate -> {
//                    Set<ConstraintViolation<EmailTemplate>> constraintViolations = validator.validate(emailTemplate);
//
//                    if (!CollectionUtils.isEmpty(constraintViolations)) {
//                        StringJoiner stringJoiner = new StringJoiner(" ");
//                        constraintViolations.forEach(
//                                loginModelConstraintViolation ->
//                                        stringJoiner
//                                                .add(loginModelConstraintViolation.getPropertyPath().toString())
//                                                .add(":")
//                                                .add(loginModelConstraintViolation.getMessage()));
//                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, stringJoiner.toString());
//                    }
//
//                    if (applicationCallbackMapping.getCallbackEndpoints().containsKey(emailTemplate.getApplicationId())) {
//                        return Mono.just(emailTemplate);
//                    } else {
//                        throw new ResponseStatusException(
//                                HttpStatus.BAD_REQUEST, "Application id is invalid");
//                    }
//                })
//                .flatMap(emailTemplate -> {
//                    String messageId = UUID.randomUUID().toString();
//                    emailTemplate.setMessageId(messageId);
//                    SenderRecord<Integer, EmailTemplate, Integer> integerEmailTemplateIntegerSenderRecord = SenderRecord.create(new ProducerRecord<>(topic, null, emailTemplate), 1);
//                    return Mono.zip(Mono.just(messageId), sender.send(Mono.just(integerEmailTemplateIntegerSenderRecord)).collectList());
//                })
//                .flatMap(TupleUtils.function((messageId, dummy) -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
//                        .body(BodyInserters.fromPublisher(Mono.just(new MessageResponse(messageId)), MessageResponse.class))));
//
//        return serverResponseMono;
//    }


    public Mono<MessageResponse> send(EmailTemplate request) {
        return Mono.just(request)
                .flatMap(emailTemplate -> {
                    Set<ConstraintViolation<EmailTemplate>> constraintViolations = validator.validate(emailTemplate);

                    if (applicationCallbackMapping.getCallbackEndpoints().containsKey(emailTemplate.getApplicationId())) {
                        return Mono.just(emailTemplate);
                    } else {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Application id is invalid");
                    }
                })
                .flatMap(emailTemplate -> {
                    String messageId = UUID.randomUUID().toString();
                    emailTemplate.setMessageId(messageId);
                    SenderRecord<Integer, EmailTemplate, Integer> integerEmailTemplateIntegerSenderRecord = SenderRecord.create(new ProducerRecord<>(topic, null, emailTemplate), 1);
                    return Mono.zip(Mono.just(messageId), sender.send(Mono.just(integerEmailTemplateIntegerSenderRecord)).collectList());
                })
                .flatMap(TupleUtils.function((messageId, dummy) -> Mono.just(new MessageResponse(messageId))));
    }
}
