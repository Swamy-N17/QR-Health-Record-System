package com.clinic.qrhealthrecord.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateContactRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,

        @NotBlank(message = "Address is required")
        String address,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact must be exactly 10 digits")
        String emergencyContact
) {}
