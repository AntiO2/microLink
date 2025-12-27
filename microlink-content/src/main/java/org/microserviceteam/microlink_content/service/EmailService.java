package org.microserviceteam.microlink_content.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {
        if (emailSender == null) {
            System.out.println("Email sender not configured. Message to " + to + ": " + subject + " - " + text);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@microlink.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
