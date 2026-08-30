package com.clinic.qrhealthrecord.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.exception.DuplicateEmailException;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.AdminRepository;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Admin registerAdmin(String fullName, String email, String rawPassword, SuperAdmin createdBy) {

        if (adminRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An admin with this email already exists: " + email);
        }

        Admin admin = new Admin();
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setCreatedBy(createdBy);

        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }
}