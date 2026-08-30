package com.clinic.qrhealthrecord.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record CreatePatientRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phoneNumber,
        @NotNull(message = "Age is required") @Min(value = 0, message = "Age must be valid") @Max(value = 130, message = "Age must be valid") Integer age,
        @NotBlank(message = "Gender is required") String gender,
        @NotBlank(message = "Address is required") String address,
        LocalDate dateOfBirth,        // optional
        String bloodGroup,            // optional
        String emergencyContact       // optional
) {}