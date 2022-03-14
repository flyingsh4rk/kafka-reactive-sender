package com.blueshark.messager.deserializer;

import com.blueshark.messager.model.EmailTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class EmailTemplateDeserializer implements Deserializer<EmailTemplate> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public EmailTemplate deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            return objectMapper.readValue(data, EmailTemplate.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing email template", e);
        }
    }
}
