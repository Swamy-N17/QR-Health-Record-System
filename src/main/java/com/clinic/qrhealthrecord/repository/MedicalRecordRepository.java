package com.clinic.qrhealthrecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.qrhealthrecord.entity.MedicalRecord;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);
    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Long doctorId);
}