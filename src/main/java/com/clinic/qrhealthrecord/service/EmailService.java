package com.clinic.qrhealthrecord.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String fromAddress;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {

        String subject = "QR Health Record System - Password Reset";

        String htmlContent = """
                <html>
                <body>
                    <p>Hello,</p>

                    <p>You requested to reset your password for the QR Health Record System.</p>

                    <p>Click the button below to reset your password:</p>

                    <p>
                        <a href="%s"
                           style="display:inline-block;
                                  padding:10px 20px;
                                  background-color:#007bff;
                                  color:white;
                                  text-decoration:none;
                                  border-radius:5px;">
                            Reset Password
                        </a>
                    </p>

                    <p>This password reset link will expire in 30 minutes.</p>

                    <p>If you did not request a password reset, you can safely ignore this email.</p>

                    <p>Regards,<br>
                    QR Health Record System</p>
                </body>
                </html>
                """.formatted(resetLink);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of(
                        "name", "QR Health Record System",
                        "email", fromAddress
                ),
                "to", new Object[]{
                        Map.of("email", toEmail)
                },
                "subject", subject,
                "htmlContent", htmlContent
        );

        restClient.post()
                .uri("/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }
}