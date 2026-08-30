package com.clinic.qrhealthrecord.dto;

import java.time.LocalDate;

import com.clinic.qrhealthrecord.entity.Doctor;

public record DoctorResponse(Long id, String doctorCode, String fullName, String email, String specialization,
                              String phoneNumber, LocalDate dateOfBirth, String gender, String address,
                              Boolean active) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getDoctorCode(),
                doctor.getFullName(),
                doctor.getEmail(),
                doctor.getSpecialization(),
                doctor.getPhoneNumber(),
                doctor.getDateOfBirth(),
                doctor.getGender(),
                doctor.getAddress(),
                doctor.getActive() == null ? true : doctor.getActive()
        );
    }
}