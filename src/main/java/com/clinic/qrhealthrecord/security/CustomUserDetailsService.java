package com.clinic.qrhealthrecord.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.repository.AdminRepository;
import com.clinic.qrhealthrecord.repository.DoctorRepository;
import com.clinic.qrhealthrecord.repository.PatientRepository;
import com.clinic.qrhealthrecord.repository.SuperAdminRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public CustomUserDetailsService(SuperAdminRepository superAdminRepository,
                                     AdminRepository adminRepository,
                                     DoctorRepository doctorRepository,
                                     PatientRepository patientRepository) {
        this.superAdminRepository = superAdminRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // SuperAdmin and Admin have no "active" flag, so they're always enabled
        Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
        if (superAdmin.isPresent()) {
            SuperAdmin sa = superAdmin.get();
            return new CustomUserDetails(sa.getId(), sa.getEmail(), sa.getPassword(), "ROLE_SUPER_ADMIN", true);
        }

        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            return new CustomUserDetails(a.getId(), a.getEmail(), a.getPassword(), "ROLE_ADMIN", true);
        }

        // Doctor and Patient carry a real active flag — null is treated as active,
        // since existing rows created before Phase 2 have no value set for it.
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) {
            Doctor d = doctor.get();
            boolean isActive = d.getActive() == null || d.getActive();
            return new CustomUserDetails(d.getId(), d.getEmail(), d.getPassword(), "ROLE_DOCTOR", isActive);
        }

        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) {
            Patient p = patient.get();
            boolean isActive = p.getActive() == null || p.getActive();
            return new CustomUserDetails(p.getId(), p.getEmail(), p.getPassword(), "ROLE_PATIENT", isActive);
        }

        throw new UsernameNotFoundException("No user found with email: " + email);
    }
}