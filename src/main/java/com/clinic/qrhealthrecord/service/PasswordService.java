package com.clinic.qrhealthrecord.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.AdminRepository;
import com.clinic.qrhealthrecord.repository.DoctorRepository;
import com.clinic.qrhealthrecord.repository.PatientRepository;
import com.clinic.qrhealthrecord.repository.SuperAdminRepository;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public PasswordService(PasswordEncoder passwordEncoder,
                           SuperAdminRepository superAdminRepository,
                           AdminRepository adminRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository) {
        this.passwordEncoder = passwordEncoder;
        this.superAdminRepository = superAdminRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public void changePassword(Long userId, String role, String currentPassword, String newPassword) {
        switch (role) {
            case "ROLE_SUPER_ADMIN" -> {
                SuperAdmin user = superAdminRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                verifyAndSet(user.getPassword(), currentPassword, newPassword, user::setPassword);
                superAdminRepository.save(user);
            }
            case "ROLE_ADMIN" -> {
                Admin user = adminRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                verifyAndSet(user.getPassword(), currentPassword, newPassword, user::setPassword);
                adminRepository.save(user);
            }
            case "ROLE_DOCTOR" -> {
                Doctor user = doctorRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                verifyAndSet(user.getPassword(), currentPassword, newPassword, user::setPassword);
                doctorRepository.save(user);
            }
            case "ROLE_PATIENT" -> {
                Patient user = patientRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                verifyAndSet(user.getPassword(), currentPassword, newPassword, user::setPassword);
                patientRepository.save(user);
            }
            default -> throw new IllegalArgumentException("Unsupported account role");
        }
    }

    private void verifyAndSet(String encodedCurrent, String currentPassword, String newPassword,
                              java.util.function.Consumer<String> setter) {
        if (!passwordEncoder.matches(currentPassword, encodedCurrent)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, encodedCurrent)) {
            throw new IllegalArgumentException("New password must be different from the current password.");
        }
        setter.accept(passwordEncoder.encode(newPassword));
    }
}
