package com.blueshark.messager.controller;

import com.blueshark.messager.handler.EmailHandler;
import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.MessageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/email")
public class MessageController {
    private final EmailHandler emailHandler;

    public MessageController(EmailHandler emailHandler) {
        this.emailHandler = emailHandler;
    }

    @PostMapping("/send")
    private Mono<MessageResponse> updateEmployee(@Valid @RequestBody EmailTemplate request) {
        return emailHandler.send(request);
    }
}
