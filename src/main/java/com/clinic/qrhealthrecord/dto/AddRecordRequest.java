package com.clinic.qrhealthrecord.dto;

import jakarta.validation.constraints.NotBlank;

public record AddRecordRequest(
        @NotBlank(message = "Diagnosis is required")
        String diagnosis,

        @NotBlank(message = "Medicine name is required")
        String medicineName,

        @NotBlank(message = "Dosage is required")
        String dosage,

        @NotBlank(message = "Frequency is required")
        String frequency,

        @NotBlank(message = "Duration is required")
        String duration,

        String instructions,

        String visitNotes
) {}
