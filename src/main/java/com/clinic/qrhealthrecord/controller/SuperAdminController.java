package com.clinic.qrhealthrecord.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.AdminResponse;
import com.clinic.qrhealthrecord.dto.CreateAdminRequest;
import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.security.CustomUserDetails;
import com.clinic.qrhealthrecord.service.AdminService;
import com.clinic.qrhealthrecord.service.DoctorService;
import com.clinic.qrhealthrecord.service.PatientService;
import com.clinic.qrhealthrecord.service.SuperAdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    private final AdminService adminService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final SuperAdminService superAdminService;

    public SuperAdminController(AdminService adminService, DoctorService doctorService,
                                 PatientService patientService, SuperAdminService superAdminService) {
        this.adminService = adminService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.superAdminService = superAdminService;
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody CreateAdminRequest requestBody,
                                                       @AuthenticationPrincipal CustomUserDetails currentUser) {

        var superAdmin = superAdminService.getSuperAdminById(currentUser.getId());

        Admin admin = adminService.registerAdmin(
                requestBody.fullName(),
                requestBody.email(),
                requestBody.password(),
                superAdmin
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(AdminResponse.from(admin));
    }

    @GetMapping("/admins")
    public List<AdminResponse> getAllAdmins() {
        return adminService.getAllAdmins().stream()
                .map(AdminResponse::from)
                .toList();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "totalAdmins", adminService.getAllAdmins().size(),
                "totalDoctors", doctorService.getAllDoctors().size(),
                "totalPatients", patientService.getAllPatients().size()
        );
    }

  
}