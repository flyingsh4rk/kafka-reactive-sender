package com.blueshark.messager.handler;

import com.blueshark.messager.config.ApplicationCallbackMapping;
import com.blueshark.messager.model.ProcessResult;
import com.blueshark.messager.model.SendingError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CallbackHandler {

    final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ApplicationCallbackMapping applicationCallbackMapping;


    public CallbackHandler(WebClient.Builder builder, ObjectMapper objectMapper, ApplicationCallbackMapping applicationCallbackMapping) {
        webClient = builder.build();
        this.objectMapper = objectMapper;
        this.applicationCallbackMapping = applicationCallbackMapping;
    }

    private URI getUri(String uri) {
        try {
            return new URI(uri);
        } catch (Exception e) {
            return null;
        }
    }

    public Mono<Object> callback(String applicationId, String messageId, List<SendingError> errors) {
        Map<String, String> callbackEndpoints = applicationCallbackMapping.getCallbackEndpoints();
        String uri = callbackEndpoints.get(applicationId);
        URI callbackEndpoint = getUri(uri);
        if (callbackEndpoint == null) {
            logger.error("Callback endpoint parsing error, application id: {}, uri: {}", applicationId, uri);
            return Mono.empty();
        }
        WebClient.RequestBodyUriSpec postSpec = webClient.post();
        List<ProcessResult.FailResponse> failResponses = Optional.ofNullable(errors).map(msgErrors -> {
            return msgErrors.stream().map(batchError -> {
                Throwable cause = batchError.getCause();
                Set<String> failedAddresses = batchError.getFails();
                return failedAddresses.stream().map(address -> new ProcessResult.FailResponse(address, cause.getMessage())).collect(Collectors.toList());
            }).flatMap(List::stream).collect(Collectors.toList());
        }).orElse(new ArrayList<>());
        ProcessResult result = new ProcessResult(errors == null ? true : false, messageId, failResponses);
        try {
            logger.info("Sending callback to application id: {}, contents: {} ", applicationId, objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            // ignore
        }
        Mono<Object> response = postSpec
                .uri(callbackEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(result), ProcessResult.class))
                .retrieve()
                .bodyToMono(Object.class);

        return response;
    }
}
