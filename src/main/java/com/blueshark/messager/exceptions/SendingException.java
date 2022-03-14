package com.blueshark.messager.exceptions;

import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.SendingError;

import java.util.List;

public class SendingException extends RuntimeException {
    private List<SendingError> errors;

    private EmailTemplate template;

    public SendingException(List<SendingError> errors, EmailTemplate messageId) {
        this.errors = errors;
        this.template = messageId;
    }

    public List<SendingError> getErrors() {
        return errors;
    }

    public void setErrors(List<SendingError> errors) {
        this.errors = errors;
    }

    public EmailTemplate getTemplate() {
        return template;
    }

    public void setTemplate(EmailTemplate template) {
        this.template = template;
    }
}
