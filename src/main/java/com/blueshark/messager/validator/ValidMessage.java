package com.blueshark.messager.validator;

import com.blueshark.messager.config.ApplicationCallbackMapping;
import com.blueshark.messager.model.EmailTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ValidMessage implements Validator {
    private final ApplicationCallbackMapping applicationCallbackMapping;

    public ValidMessage(ApplicationCallbackMapping applicationCallbackMapping) {
        this.applicationCallbackMapping = applicationCallbackMapping;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailTemplate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EmailTemplate request = (EmailTemplate) target;
        if (!applicationCallbackMapping.getCallbackEndpoints().containsKey(request.getApplicationId())) {
            errors.rejectValue(
                    "applicationId",
                    null,null, null);
        }
    }
}