package com.clinic.qrhealthrecord.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.repository.SuperAdminRepository;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminInitializer(SuperAdminRepository superAdminRepository, PasswordEncoder passwordEncoder) {
        this.superAdminRepository = superAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (superAdminRepository.count() == 0) {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setFullName("System Super Admin");
            superAdmin.setEmail("superadmin@clinic.com");
            superAdmin.setPassword(passwordEncoder.encode("Admin@123"));

            superAdminRepository.save(superAdmin);

            System.out.println("=================================================");
            System.out.println("Default Super Admin created:");
            System.out.println("Email: superadmin@clinic.com");
            System.out.println("Password: Admin@123");
            System.out.println("=================================================");
        }
    }
}