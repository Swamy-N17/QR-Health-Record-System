package com.clinic.qrhealthrecord.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.qrhealthrecord.entity.DoctorPatientAccess;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.repository.DoctorPatientAccessRepository;
import com.clinic.qrhealthrecord.repository.DoctorRepository;
import com.clinic.qrhealthrecord.repository.PatientRepository;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;

@Service
public class DoctorPatientAccessService {

    private final DoctorPatientAccessRepository accessRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DoctorPatientAccessService(DoctorPatientAccessRepository accessRepository,
                                      DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.accessRepository = accessRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public void recordAccess(Long doctorId, Long patientId) {
        LocalDate today = LocalDate.now();
        if (accessRepository.existsByDoctorIdAndPatientIdAndAccessDate(doctorId, patientId, today)) {
            return;
        }

        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        accessRepository.save(new DoctorPatientAccess(doctor, patient, today));
    }

    public long getTodayCount(Long doctorId) {
        return accessRepository.countByDoctorIdAndAccessDate(doctorId, LocalDate.now());
    }
}
