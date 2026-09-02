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

    private static final String FRONTEND_RESET_URL =
            "https://qr-health-record-system.onrender.com/reset-password.html";

    public PasswordResetController(
            PasswordResetTokenService passwordResetTokenService,
            EmailService emailService) {

        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest requestBody) {

        // Always return the same generic message to prevent email enumeration.
        String genericMessage =
                "If an account with that email exists, a reset link has been sent.";

        try {
            String token =
                    passwordResetTokenService.createResetToken(requestBody.email());

            String resetLink =
                    FRONTEND_RESET_URL + "?token=" + token;

            emailService.sendPasswordResetEmail(
                    requestBody.email(),
                    resetLink
            );

        } catch (RuntimeException e) {
            System.err.println("Password reset email failed: " + e.getMessage());
            e.printStackTrace();
        }

        return Map.of("message", genericMessage);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest requestBody) {

        passwordResetTokenService.resetPassword(
                requestBody.token(),
                requestBody.newPassword()
        );

        return Map.of(
                "message",
                "Password has been reset successfully. You can now log in."
        );
    }
}