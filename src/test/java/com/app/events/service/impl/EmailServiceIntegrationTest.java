package com.app.events.service.impl;

import com.app.events.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendRealEmail_shouldSendEmailToRecipient() {
        String to = "test@gmail.com";
        String subject = "Integration Test Email";
        System.out.println("Attempting to send real email to: " + to);
        String htmlBody = "<h1>Hello</h1>";
        emailService.sendHtmlMessage(to, subject, htmlBody, null);
        System.out.println("Email send command executed.");
    }
}
