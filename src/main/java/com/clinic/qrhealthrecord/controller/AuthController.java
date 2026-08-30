package com.clinic.qrhealthrecord.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.ChangePasswordRequest;
import com.clinic.qrhealthrecord.dto.LoginRequest;
import com.clinic.qrhealthrecord.security.CustomUserDetails;
import com.clinic.qrhealthrecord.service.PasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordService passwordService;

    public AuthController(AuthenticationManager authenticationManager,
                          PasswordService passwordService) {
        this.authenticationManager = authenticationManager;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();

            // Check selected role BEFORE creating the session
            if (!userDetails.getRole().equals(loginRequest.role())) {
                return ResponseEntity.status(401)
                        .body(Map.of(
                                "error",
                                "Selected role does not match this account."
                        ));
            }

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository
                            .SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "email", userDetails.getUsername(),
                            "role", userDetails.getRole()
                    )
            );

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "error",
                            "Invalid email or password"
                    ));
        }
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @Valid @RequestBody ChangePasswordRequest requestBody,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            CustomUserDetails currentUser) {

        passwordService.changePassword(
                currentUser.getId(),
                currentUser.getRole(),
                requestBody.currentPassword(),
                requestBody.newPassword()
        );

        return Map.of(
                "message",
                "Password changed successfully."
        );
    }
}