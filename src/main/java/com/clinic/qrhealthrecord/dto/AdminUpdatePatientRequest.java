package com.clinic.qrhealthrecord.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// patientCode is deliberately absent — Admin can edit everything else, but never the code
public record AdminUpdatePatientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @NotBlank(message = "Blood group is required")
        String bloodGroup,

        @NotBlank(message = "Address is required")
        String address,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact must be exactly 10 digits")
        String emergencyContact
) {}