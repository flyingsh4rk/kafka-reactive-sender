package com.blueshark.messager.model;

import java.util.List;

public class ProcessResult {
    private Boolean success;
    private String messageId;
    private List<FailResponse> fails;

    public ProcessResult(Boolean success, String messageId, List<FailResponse> fails) {
        this.success = success;
        this.messageId = messageId;
        this.fails = fails;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public List<FailResponse> getFails() {
        return fails;
    }

    public void setFails(List<FailResponse> fails) {
        this.fails = fails;
    }

    public static class FailResponse {
        private String address;
        private String cause;

        public FailResponse(String address, String cause) {
            this.address = address;
            this.cause = cause;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }
    }
}
