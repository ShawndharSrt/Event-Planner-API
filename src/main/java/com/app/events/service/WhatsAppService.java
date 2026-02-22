package com.app.events.service;

import java.util.List;

public interface WhatsAppService {

    /**
     * Send a simple WhatsApp message to a single number.
     *
     * @param to      Recipient phone number
     * @param message Text message content
     */
    void sendMessage(String to, String message);

    /**
     * Send a WhatsApp message to multiple numbers.
     *
     * @param toNumbers List of recipient phone numbers
     * @param message   Text message content
     */
    void sendMessages(List<String> toNumbers, String message);

}
