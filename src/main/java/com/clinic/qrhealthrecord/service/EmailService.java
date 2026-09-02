package com.clinic.qrhealthrecord.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com")
                .build();
    }

    public void sendPasswordResetEmail(String recipientEmail, String resetLink) {

        String emailBody =
                "You requested a password reset.\n\n" +
                "Click the link below to set a new password (valid for 30 minutes):\n\n" +
                resetLink + "\n\n" +
                "If you did not request this, please ignore this email.";

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of(
                        "name", "QR Health Record System",
                        "email", senderEmail
                ),
                "to", new Object[]{
                        Map.of("email", recipientEmail)
                },
                "subject", "Password Reset - QR Health Record System",
                "textContent", emailBody
        );

        restClient.post()
                .uri("/v3/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }
}