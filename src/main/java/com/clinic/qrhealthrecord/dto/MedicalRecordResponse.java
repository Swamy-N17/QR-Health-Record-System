package com.clinic.qrhealthrecord.dto;

import java.time.LocalDateTime;

import com.clinic.qrhealthrecord.entity.MedicalRecord;

public record MedicalRecordResponse(
        Long id, String diagnosis, String prescription, String medicineName, String dosage,
        String frequency, String duration, String instructions, String visitNotes,
        LocalDateTime visitDate, String doctorName, String doctorCode,
        String patientName, String patientCode) {

    public static MedicalRecordResponse from(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getMedicineName(),
                record.getDosage(),
                record.getFrequency(),
                record.getDuration(),
                record.getInstructions(),
                record.getVisitNotes(),
                record.getVisitDate(),
                record.getDoctor().getFullName(),
                record.getDoctor().getDoctorCode(),
                record.getPatient().getFullName(),
                record.getPatient().getPatientCode()
        );
    }
}
