package com.blueshark.messager.sender;

import com.blueshark.messager.model.EmailTemplate;
import com.blueshark.messager.model.MessageType;
import com.blueshark.messager.model.SendingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class EmailSender implements Sender {

    final private Session session;

    private final Integer receiverBatch;

    final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    public EmailSender(@Value("${configuration.email.server.host}") String host
            , @Value("${configuration.email.server.port}") String port
            , @Value("${configuration.email.sending-batch}") Integer receiverBatch) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        this.receiverBatch = receiverBatch;
        this.session = Session.getDefaultInstance(properties);
    }

    @Override
    public MessageType getAcceptType() {
        return MessageType.EMAIL;
    }

    @Override
    public List<SendingError> send(EmailTemplate emailTemplate) {
        List<String> receivers = emailTemplate.getReceivers();
        List<SendingError> errors = new ArrayList<>();
        int totalReceiver = receivers.size();
        for (int i = 0; i < totalReceiver; i += receiverBatch) {
            int maxIndex = i + receiverBatch <= totalReceiver ? i + receiverBatch : totalReceiver;
            List<String> receiverBatch = receivers.subList(i, maxIndex);
            List<Address> addressList = new ArrayList<>();
            try {
                for (String address : receiverBatch) {
                    addressList.add(new InternetAddress(address));
                }
                send(emailTemplate, addressList);
            } catch (Exception e) {
                logger.error("Sending email error for receiver: [{}]", receiverBatch.stream().collect(Collectors.joining(";")), e);
                SendingError sendingError = new SendingError(new HashSet<>(receiverBatch), e);
                errors.add(sendingError);
            }
        }
        return errors;
    }

    private void send(EmailTemplate emailTemplate, List<Address> addresses) throws MessagingException {
        MimeMessage message = new MimeMessage(this.session);
        message.setContent(emailTemplate.getBody(), "text/html; charset=utf-8");
        message.setFrom(new InternetAddress(emailTemplate.getFrom()));
        message.addRecipients(Message.RecipientType.TO, addresses.toArray(new Address[0]));
        message.setSubject(emailTemplate.getTitle());
        Transport.send(message);
    }
}
