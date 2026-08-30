package com.clinic.qrhealthrecord.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.MedicalRecordResponse;
import com.clinic.qrhealthrecord.dto.PatientSelfUpdateRequest;
import com.clinic.qrhealthrecord.dto.PatientResponse;
import com.clinic.qrhealthrecord.entity.MedicalRecord;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.security.CustomUserDetails;
import com.clinic.qrhealthrecord.service.MedicalRecordService;
import com.clinic.qrhealthrecord.service.PatientService;
import com.clinic.qrhealthrecord.util.HealthCardGenerator;
import com.clinic.qrhealthrecord.util.QrCodeGenerator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;
    private final MedicalRecordService medicalRecordService;
    private final QrCodeGenerator qrCodeGenerator;
    private final HealthCardGenerator healthCardGenerator;

    public PatientController(PatientService patientService, MedicalRecordService medicalRecordService,
                             QrCodeGenerator qrCodeGenerator, HealthCardGenerator healthCardGenerator) {
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;
        this.qrCodeGenerator = qrCodeGenerator;
        this.healthCardGenerator = healthCardGenerator;
    }

    // ---- Profile ----

    @GetMapping("/profile")
    public PatientResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return PatientResponse.from(patientService.getPatientById(currentUser.getId()));
    }

    @PutMapping("/profile")
    public PatientResponse updateMyProfile(@Valid @RequestBody PatientSelfUpdateRequest requestBody,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        Patient updated = patientService.updateOwnProfile(
                currentUser.getId(), requestBody.fullName(), requestBody.email(), requestBody.phoneNumber(),
                requestBody.age(), requestBody.dateOfBirth(), requestBody.gender(), requestBody.address(),
                requestBody.emergencyContact());
        return PatientResponse.from(updated);
    }

    // ---- QR Code ----
    // Generated fresh, in memory, every time it's requested.

    @GetMapping("/qr-code")
    public ResponseEntity<byte[]> getMyQrCode(@AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        Patient patient = patientService.getPatientById(currentUser.getId());
        byte[] qrBytes = qrCodeGenerator.generateQrCodeBytes(patient.getPatientCode());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + patient.getPatientCode() + ".png\"")
                .body(qrBytes);
    }

    // ---- Health Card ----

    @GetMapping("/health-card")
    public ResponseEntity<byte[]> getMyHealthCard(
            @RequestParam(defaultValue = "false") boolean download,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {

        Patient patient = patientService.getPatientById(currentUser.getId());
        byte[] cardBytes = healthCardGenerator.generateHealthCard(patient);

        String disposition = download ? "attachment" : "inline";

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + patient.getPatientCode() + "-health-card.png\"")
                .body(cardBytes);
    }

    // ---- Medical History ----

    @GetMapping("/history")
    public List<MedicalRecordResponse> getMyMedicalHistory(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return medicalRecordService.getHistoryForPatient(currentUser.getId()).stream()
                .map(MedicalRecordResponse::from)
                .toList();
    }

    // ---- Dashboard ----

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats(@AuthenticationPrincipal CustomUserDetails currentUser) {

        Patient patient = patientService.getPatientById(currentUser.getId());
        List<MedicalRecord> history = medicalRecordService.getHistoryForPatient(currentUser.getId());

        List<MedicalRecordResponse> recentPrescriptions = history.stream()
                .limit(3)
                .map(MedicalRecordResponse::from)
                .toList();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("personalInfo", PatientResponse.from(patient));
        dashboard.put("medicalHistoryCount", history.size());
        dashboard.put("recentPrescriptions", recentPrescriptions);

        return dashboard;
    }
}
