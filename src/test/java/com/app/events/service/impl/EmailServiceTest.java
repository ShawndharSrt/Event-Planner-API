package com.app.events.service.impl;

import com.app.events.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SmtpEmailService emailService;

    @Test
    void sendSimpleMessage_shouldSendEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        // When
        emailService.sendSimpleMessage(to, subject, text);

        // Then
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlMessage_shouldSendMimeMessage() {
        // Given
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        String to = "recipient@example.com";
        String subject = "HTML Subject";
        String htmlBody = "<h1>Hello</h1>";

        // When
        emailService.sendHtmlMessage(to, subject, htmlBody, null);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
