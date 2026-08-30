package com.clinic.qrhealthrecord.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.exception.DuplicateEmailException;
import com.clinic.qrhealthrecord.exception.DuplicateContactException;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Doctor registerDoctor(String fullName, String email, String rawPassword, String specialization,
                                  String phoneNumber, LocalDate dateOfBirth, String gender, String address,
                                  Admin createdBy) {

        if (doctorRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A doctor with this email already exists: " + email);
        }

        if (doctorRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateContactException("A doctor with this phone number already exists: " + phoneNumber);
        }

        // Step 1: Save without the doctorCode, so Hibernate assigns an id
        Doctor doctor = new Doctor();
        doctor.setFullName(fullName);
        doctor.setEmail(email);
        doctor.setPassword(passwordEncoder.encode(rawPassword));
        doctor.setSpecialization(specialization);
        doctor.setPhoneNumber(phoneNumber);
        doctor.setDateOfBirth(dateOfBirth);
        doctor.setGender(gender);
        doctor.setAddress(address);
        doctor.setCreatedBy(createdBy);

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Step 2: Now that we have the id, generate the formatted code
        String generatedCode = "DOC" + String.format("%04d", savedDoctor.getId());
        savedDoctor.setDoctorCode(generatedCode);

        // Step 3: Save again — this time Hibernate performs an UPDATE, not an INSERT
        return doctorRepository.save(savedDoctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    public Doctor getDoctorByCode(String doctorCode) {
        return doctorRepository.findByDoctorCode(doctorCode)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with code: " + doctorCode));
    }

    // Admin can update almost everything except doctorCode
    public Doctor updateDoctorByAdmin(Long doctorId, String fullName, String email, String phoneNumber,
                                       String address, String specialization) {
        Doctor doctor = getDoctorById(doctorId);

        if (!doctor.getEmail().equalsIgnoreCase(email) && doctorRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A doctor with this email already exists: " + email);
        }

        if (doctorRepository.existsByPhoneNumberAndIdNot(phoneNumber, doctorId)) {
            throw new DuplicateContactException("A doctor with this phone number already exists: " + phoneNumber);
        }

        doctor.setFullName(fullName);
        doctor.setEmail(email);
        doctor.setPhoneNumber(phoneNumber);
        doctor.setAddress(address);
        doctor.setSpecialization(specialization);
        return doctorRepository.save(doctor);
    }

    // Doctor can update personal/contact information, but doctorCode and specialization remain protected.
    public Doctor updateOwnProfile(Long doctorId, String fullName, String email, String phoneNumber,
                                   LocalDate dateOfBirth, String gender, String address) {
        Doctor doctor = getDoctorById(doctorId);

        if (!doctor.getEmail().equalsIgnoreCase(email) && doctorRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("A doctor with this email already exists: " + email);
        }

        if (doctorRepository.existsByPhoneNumberAndIdNot(phoneNumber, doctorId)) {
            throw new DuplicateContactException("A doctor with this phone number already exists: " + phoneNumber);
        }

        doctor.setFullName(fullName);
        doctor.setEmail(email);
        doctor.setPhoneNumber(phoneNumber);
        doctor.setDateOfBirth(dateOfBirth);
        doctor.setGender(gender);
        doctor.setAddress(address);
        return doctorRepository.save(doctor);
    }

    public Doctor updateStatus(Long doctorId, boolean active) {
        Doctor doctor = getDoctorById(doctorId);
        doctor.setActive(active);
        return doctorRepository.save(doctor);
    }
}