package com.clinic.qrhealthrecord.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.qrhealthrecord.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    boolean existsByEmergencyContact(String emergencyContact);
    boolean existsByEmergencyContactAndIdNot(String emergencyContact, Long id);
    Optional<Patient> findByPatientCode(String patientCode);
}