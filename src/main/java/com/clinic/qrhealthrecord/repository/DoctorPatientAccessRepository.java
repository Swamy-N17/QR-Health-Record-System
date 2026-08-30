package com.clinic.qrhealthrecord.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.qrhealthrecord.entity.DoctorPatientAccess;

public interface DoctorPatientAccessRepository extends JpaRepository<DoctorPatientAccess, Long> {
    boolean existsByDoctorIdAndPatientIdAndAccessDate(Long doctorId, Long patientId, LocalDate accessDate);
    long countByDoctorIdAndAccessDate(Long doctorId, LocalDate accessDate);
}
