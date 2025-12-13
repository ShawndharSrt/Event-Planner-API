package com.app.events.service;

import java.io.File;
import java.util.Map;

public interface EmailService {

    /**
     * Send a simple text email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email body text
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Send an HTML email with optional attachments.
     *
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param htmlBody    HTML content
     * @param attachments Map of filename to File object for attachments (can be
     *                    null)
     */
    void sendHtmlMessage(String to, String subject, String htmlBody, Map<String, File> attachments);
}
