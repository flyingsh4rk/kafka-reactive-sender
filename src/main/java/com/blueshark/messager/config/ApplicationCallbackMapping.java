package com.blueshark.messager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "configuration")
public class ApplicationCallbackMapping {

    private Map<String, String> callbackEndpoints;

    public ApplicationCallbackMapping(Map<String, String> callbackEndpoints) {
        this.callbackEndpoints = callbackEndpoints;
    }

    public Map<String, String> getCallbackEndpoints() {
        return callbackEndpoints;
    }

    public void setCallbackEndpoints(Map<String, String> callbackEndpoints) {
        this.callbackEndpoints = callbackEndpoints;
    }
}
