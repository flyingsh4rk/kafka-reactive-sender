package com.blueshark.messager.model;

import java.util.Set;

public class SendingError {

    private Set<String> fails;
    private Throwable cause;

    public SendingError(Set<String> fails, Throwable cause) {
        this.fails = fails;
        this.cause = cause;
    }

    public Set<String> getFails() {
        return fails;
    }

    public void setFails(Set<String> fails) {
        this.fails = fails;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
