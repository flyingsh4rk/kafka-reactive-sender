package com.blueshark.messager.sender;

import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.MessageType;
import com.blueshark.messager.model.SendingError;

import java.util.List;

public interface Sender {
    MessageType getAcceptType();

    List<SendingError> send(EmailTemplate emailTemplate);
}
