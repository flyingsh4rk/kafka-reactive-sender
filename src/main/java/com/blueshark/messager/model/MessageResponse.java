package com.blueshark.messager.model;

public class MessageResponse {
    private String messageId;

    public MessageResponse(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}
