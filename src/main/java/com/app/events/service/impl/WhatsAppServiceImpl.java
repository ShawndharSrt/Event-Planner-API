package com.app.events.service.impl;

import com.app.events.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${whatsapp.provider:mock}")
    private String provider;

    @Value("${whatsapp.api.url:}")
    private String apiUrl;

    @Value("${whatsapp.api.key:}")
    private String apiKey;

    @Override
    @Async
    public void sendMessage(String to, String message) {
        try {
            // Check if phone number is valid
            if (to == null || to.trim().isEmpty()) {
                log.warn("Cannot send WhatsApp message. Phone number is missing.");
                return;
            }

            if ("mock".equalsIgnoreCase(provider)) {
                log.info("MOCK WhatsApp to {}: {}", to, message);
                return;
            }

            // TODO: Implement actual provider integration (Twilio / Meta API)
            // using the injected apiUrl and apiKey
            log.info("Sending WhatsApp via provider [{}] to {}. Message length: {}", provider, to, message.length());

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendMessages(List<String> toNumbers, String message) {
        if (toNumbers == null || toNumbers.isEmpty()) {
            return;
        }
        for (String to : toNumbers) {
            sendMessage(to, message);
        }
    }
}
