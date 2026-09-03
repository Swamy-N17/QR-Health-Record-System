package com.clinic.qrhealthrecord.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.PasswordResetToken;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.repository.AdminRepository;
import com.clinic.qrhealthrecord.repository.DoctorRepository;
import com.clinic.qrhealthrecord.repository.PasswordResetTokenRepository;
import com.clinic.qrhealthrecord.repository.PatientRepository;
import com.clinic.qrhealthrecord.repository.SuperAdminRepository;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRY_MINUTES = 30;

    public PasswordResetTokenService(PasswordResetTokenRepository tokenRepository,
                                      SuperAdminRepository superAdminRepository,
                                      AdminRepository adminRepository,
                                      DoctorRepository doctorRepository,
                                      PatientRepository patientRepository,
                                      PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.superAdminRepository = superAdminRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createResetToken(String email) {

        String generatedToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        PasswordResetToken resetToken =
                new PasswordResetToken(generatedToken, expiry);

        Optional<SuperAdmin> superAdmin =
                superAdminRepository.findByEmail(email);

        Optional<Admin> admin =
                adminRepository.findByEmail(email);

        Optional<Doctor> doctor =
                doctorRepository.findByEmail(email);

        Optional<Patient> patient =
                patientRepository.findByEmail(email);

        if (superAdmin.isPresent()) {
            resetToken.setSuperAdmin(superAdmin.get());

        } else if (admin.isPresent()) {
            resetToken.setAdmin(admin.get());

        } else if (doctor.isPresent()) {
            resetToken.setDoctor(doctor.get());

        } else if (patient.isPresent()) {
            resetToken.setPatient(patient.get());

        } else {
            throw new RuntimeException(
                    "No account found with this email: " + email
            );
        }

        tokenRepository.save(resetToken);

        return generatedToken;
    }

    public void resetPassword(String token, String newRawPassword) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid or expired reset token"
                                ));

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            tokenRepository.delete(resetToken);

            throw new RuntimeException(
                    "This reset link has expired. Please request a new one."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(newRawPassword);

        if (resetToken.getSuperAdmin() != null) {

            SuperAdmin sa = resetToken.getSuperAdmin();

            sa.setPassword(encodedPassword);

            superAdminRepository.save(sa);

            System.out.println(
                    "PASSWORD RESET: SuperAdmin password updated for ID "
                            + sa.getId()
            );

        } else if (resetToken.getAdmin() != null) {

            Admin a = resetToken.getAdmin();

            a.setPassword(encodedPassword);

            adminRepository.save(a);

            System.out.println(
                    "PASSWORD RESET: Admin password updated for ID "
                            + a.getId()
            );

        } else if (resetToken.getDoctor() != null) {

            Doctor d = resetToken.getDoctor();

            d.setPassword(encodedPassword);

            doctorRepository.save(d);

            System.out.println(
                    "PASSWORD RESET: Doctor password updated for ID "
                            + d.getId()
                            + ", email " + d.getEmail()
            );

        } else if (resetToken.getPatient() != null) {

            Patient p = resetToken.getPatient();

            p.setPassword(encodedPassword);

            patientRepository.save(p);

            System.out.println(
                    "PASSWORD RESET: Patient password updated for ID "
                            + p.getId()
                            + ", email " + p.getEmail()
            );
        }

        tokenRepository.delete(resetToken);
    }
}