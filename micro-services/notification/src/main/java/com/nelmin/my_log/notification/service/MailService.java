package com.nelmin.my_log.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.emulator:false}")
    private Boolean emulator;

    @Value("${spring.mail.sender:blog@nelmin.com}")
    private String senderName;

    private static final String CONTENT_TYPE = "text/html; charset=utf-8";

    private final JavaMailSender javaMailSender;

    public void sendMail(String destination, String subject, String content) {

        if (!StringUtils.hasText(destination)) {
            log.info("Destination is empty, skip sending email");
            return;
        }

        log.info("Send email to {}", destination);

        if (emulator) {
            log.info("Emulator enabled, skip sending email");
            log.info("Subject : {}", subject);
            log.info("Content : {}", content);
            return;
        }

        var message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderName);
            message.setRecipients(MimeMessage.RecipientType.TO, destination);
            message.setSubject(subject);
            message.setContent(content, CONTENT_TYPE);
        } catch (MessagingException e) {
            log.error("Error create message", e);
            return;
        }

        javaMailSender.send(message);
    }
}
