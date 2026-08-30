package com.clinic.qrhealthrecord.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PatientSelfUpdateRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be valid")
        @Max(value = 130, message = "Age must be valid")
        Integer age,

        LocalDate dateOfBirth,

        @NotBlank(message = "Gender is required")
        String gender,

        @NotBlank(message = "Address is required")
        String address,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact must be exactly 10 digits")
        String emergencyContact
) {}
