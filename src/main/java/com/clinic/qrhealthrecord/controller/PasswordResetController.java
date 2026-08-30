package com.clinic.qrhealthrecord.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.ForgotPasswordRequest;
import com.clinic.qrhealthrecord.dto.ResetPasswordRequest;
import com.clinic.qrhealthrecord.service.EmailService;
import com.clinic.qrhealthrecord.service.PasswordResetTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    private static final String FRONTEND_RESET_URL = "http://localhost:8080/reset-password.html";

    public PasswordResetController(PasswordResetTokenService passwordResetTokenService, EmailService emailService) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest requestBody) {

        // Always return the same generic message, regardless of whether the email
        // exists or not — this prevents attackers from probing which emails are registered.
        String genericMessage = "If an account with that email exists, a reset link has been sent.";

        try {
            String token = passwordResetTokenService.createResetToken(requestBody.email());

            String resetLink = FRONTEND_RESET_URL + "?token=" + token;
            String emailBody = "You requested a password reset.\n\n" +
                    "Click the link below to set a new password (valid for 30 minutes):\n\n" +
                    resetLink + "\n\n" +
                    "If you did not request this, please ignore this email.";

            emailService.sendEmail(requestBody.email(), "Password Reset - QR Health Record System", emailBody);

        } catch (RuntimeException e) {
            // Email not found — silently ignore, per the generic-message security practice above
        }

        return Map.of("message", genericMessage);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest requestBody) {
        passwordResetTokenService.resetPassword(requestBody.token(), requestBody.newPassword());
        return Map.of("message", "Password has been reset successfully. You can now log in.");
    }

  
}