
package com.clinic.qrhealthrecord.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// doctorCode is deliberately absent — Admin can edit everything else, but never the code
public record AdminUpdateDoctorRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Specialization is required")
        String specialization
) {}