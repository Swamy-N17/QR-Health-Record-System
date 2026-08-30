package com.clinic.qrhealthrecord.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.exception.DuplicateEmailException;
import com.clinic.qrhealthrecord.exception.DuplicateContactException;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Patient registerPatient(String fullName, String email, String rawPassword, String phoneNumber,
            Integer age, String gender, String address, LocalDate dateOfBirth,
            String bloodGroup, String emergencyContact, Admin createdBy) {

        if (patientRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A patient with this email already exists: " + email);
        }

        if (patientRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateContactException("A patient with this phone number already exists: " + phoneNumber);
        }

        if (emergencyContact != null && !emergencyContact.isBlank()
                && patientRepository.existsByEmergencyContact(emergencyContact)) {
            throw new DuplicateContactException("A patient with this emergency contact already exists: " + emergencyContact);
        }

        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPassword(passwordEncoder.encode(rawPassword));
        patient.setPhoneNumber(phoneNumber);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setDateOfBirth(dateOfBirth);       // may be null
        patient.setBloodGroup(bloodGroup);         // may be null
        patient.setEmergencyContact(emergencyContact);
        patient.setCreatedBy(createdBy);

        Patient savedPatient = patientRepository.save(patient);

        // Format the patient code now that we have a real id.
        String generatedCode = "PAT" + String.format("%04d", savedPatient.getId());
        savedPatient.setPatientCode(generatedCode);

        // NOTE: QR code is no longer generated or stored here.
        // It's generated on-demand, in memory, whenever it's actually
        // requested (see PatientController.downloadMyQrCode). This avoids
        // relying on a local filesystem, which isn't reliable once deployed.

        return patientRepository.save(savedPatient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public Patient getPatientByCode(String patientCode) {
        return patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with code: " + patientCode));
    }

    // Admin can update almost everything except patientCode
    public Patient updatePatientByAdmin(Long patientId, String fullName, String email, String phoneNumber,
                                         LocalDate dateOfBirth, String bloodGroup, String address,
                                         String emergencyContact) {
        Patient patient = getPatientById(patientId);

        if (!patient.getEmail().equalsIgnoreCase(email) && patientRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A patient with this email already exists: " + email);
        }

        if (patientRepository.existsByPhoneNumberAndIdNot(phoneNumber, patientId)) {
            throw new DuplicateContactException("A patient with this phone number already exists: " + phoneNumber);
        }

        if (emergencyContact != null && !emergencyContact.isBlank()
                && patientRepository.existsByEmergencyContactAndIdNot(emergencyContact, patientId)) {
            throw new DuplicateContactException("A patient with this emergency contact already exists: " + emergencyContact);
        }

        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPhoneNumber(phoneNumber);
        patient.setDateOfBirth(dateOfBirth);
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        return patientRepository.save(patient);
    }

    // Patient can update personal/contact information, but blood group and patientCode remain protected.
    public Patient updateOwnProfile(Long patientId, String fullName, String email, String phoneNumber,
                                    Integer age, LocalDate dateOfBirth, String gender, String address,
                                    String emergencyContact) {
        Patient patient = getPatientById(patientId);

        if (!patient.getEmail().equalsIgnoreCase(email) && patientRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A patient with this email already exists: " + email);
        }

        if (patientRepository.existsByPhoneNumberAndIdNot(phoneNumber, patientId)) {
            throw new DuplicateContactException("A patient with this phone number already exists: " + phoneNumber);
        }

        if (emergencyContact != null && !emergencyContact.isBlank()
                && patientRepository.existsByEmergencyContactAndIdNot(emergencyContact, patientId)) {
            throw new DuplicateContactException("A patient with this emergency contact already exists: " + emergencyContact);
        }

        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPhoneNumber(phoneNumber);
        patient.setAge(age);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        return patientRepository.save(patient);
    }

    public Patient updateStatus(Long patientId, boolean active) {
        Patient patient = getPatientById(patientId);
        patient.setActive(active);
        return patientRepository.save(patient);
    }
}