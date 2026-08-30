package com.clinic.qrhealthrecord.dto;

import java.time.LocalDate;

import com.clinic.qrhealthrecord.entity.Patient;

public record PatientResponse(Long id, String patientCode, String fullName, String email, String phoneNumber,
        Integer age, LocalDate dateOfBirth, String gender, String bloodGroup,
        String address, String emergencyContact, Boolean active) {

public static PatientResponse from(Patient patient) {
return new PatientResponse(
patient.getId(), patient.getPatientCode(), patient.getFullName(), patient.getEmail(),
patient.getPhoneNumber(), patient.getAge(), patient.getDateOfBirth(),
patient.getGender(), patient.getBloodGroup(), patient.getAddress(), patient.getEmergencyContact(),
patient.getActive() == null ? true : patient.getActive()
);
}
}