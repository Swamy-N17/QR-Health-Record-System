package com.clinic.qrhealthrecord.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.security.CustomUserDetails;

@RestController
@RequestMapping("/api/auth")
public class AuthStatusController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return Map.of(
                "id", currentUser.getId(),
                "email", currentUser.getUsername(),
                "role", currentUser.getRole()
        );
    }
}