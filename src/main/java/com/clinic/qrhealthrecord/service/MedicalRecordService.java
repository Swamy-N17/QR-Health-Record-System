package com.clinic.qrhealthrecord.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.MedicalRecord;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.DoctorRepository;
import com.clinic.qrhealthrecord.repository.MedicalRecordRepository;
import com.clinic.qrhealthrecord.repository.PatientRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, PatientRepository patientRepository,
                                 DoctorRepository doctorRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public MedicalRecord addMedicalRecord(Long patientId, Long doctorId, String diagnosis,
                                          String medicineName, String dosage, String frequency,
                                          String duration, String instructions, String visitNotes) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setDiagnosis(diagnosis);
        record.setMedicineName(medicineName);
        record.setDosage(dosage);
        record.setFrequency(frequency);
        record.setDuration(duration);
        record.setInstructions(instructions);
        record.setPrescription(buildPrescriptionSummary(medicineName, dosage, frequency, duration, instructions));
        record.setVisitNotes(visitNotes);
        record.setVisitDate(LocalDateTime.now());

        return medicalRecordRepository.save(record);
    }

    private String buildPrescriptionSummary(String medicineName, String dosage, String frequency,
                                             String duration, String instructions) {
        StringBuilder summary = new StringBuilder();
        summary.append("Medicine: ").append(medicineName);
        summary.append("\nDosage: ").append(dosage);
        summary.append("\nFrequency: ").append(frequency);
        summary.append("\nDuration: ").append(duration);
        if (instructions != null && !instructions.isBlank()) {
            summary.append("\nInstructions: ").append(instructions.trim());
        }
        return summary.toString();
    }

    public List<MedicalRecord> getHistoryForPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    public List<MedicalRecord> getRecordsByDoctor(Long doctorId) {
        return medicalRecordRepository.findByDoctorIdOrderByVisitDateDesc(doctorId);
    }
}
