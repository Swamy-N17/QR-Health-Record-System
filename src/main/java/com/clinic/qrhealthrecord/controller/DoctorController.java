package com.clinic.qrhealthrecord.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.AddRecordRequest;
import com.clinic.qrhealthrecord.dto.DoctorResponse;
import com.clinic.qrhealthrecord.dto.DoctorSelfUpdateRequest;
import com.clinic.qrhealthrecord.dto.MedicalRecordResponse;
import com.clinic.qrhealthrecord.dto.PatientResponse;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.MedicalRecord;
import com.clinic.qrhealthrecord.security.CustomUserDetails;
import com.clinic.qrhealthrecord.service.DoctorPatientAccessService;
import com.clinic.qrhealthrecord.service.DoctorService;
import com.clinic.qrhealthrecord.service.MedicalRecordService;
import com.clinic.qrhealthrecord.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final PatientService patientService;
    private final MedicalRecordService medicalRecordService;
    private final DoctorService doctorService;
    private final DoctorPatientAccessService accessService;

    public DoctorController(PatientService patientService, MedicalRecordService medicalRecordService,
                            DoctorService doctorService, DoctorPatientAccessService accessService) {
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;
        this.doctorService = doctorService;
        this.accessService = accessService;
    }

    @GetMapping("/profile")
    public DoctorResponse getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return DoctorResponse.from(doctorService.getDoctorById(currentUser.getId()));
    }

    @PutMapping("/profile")
    public DoctorResponse updateMyProfile(@Valid @RequestBody DoctorSelfUpdateRequest requestBody,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        Doctor doctor = doctorService.updateOwnProfile(currentUser.getId(), requestBody.fullName(), requestBody.email(),
                requestBody.phoneNumber(), requestBody.dateOfBirth(), requestBody.gender(), requestBody.address());
        return DoctorResponse.from(doctor);
    }

    @GetMapping("/patients/scan/{patientCode}")
    public PatientResponse findPatientByQrCode(@PathVariable String patientCode,
                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        PatientResponse patient = PatientResponse.from(patientService.getPatientByCode(patientCode));
        accessService.recordAccess(currentUser.getId(), patient.id());
        return patient;
    }

    @GetMapping("/patients/search")
    public List<PatientResponse> searchPatients() {
        return patientService.getAllPatients().stream().map(PatientResponse::from).toList();
    }

    @GetMapping("/patients/{patientId}/history")
    public List<MedicalRecordResponse> getPatientHistory(@PathVariable Long patientId) {
        return medicalRecordService.getHistoryForPatient(patientId).stream()
                .map(MedicalRecordResponse::from).toList();
    }

    @PostMapping("/patients/{patientId}/records")
    public ResponseEntity<MedicalRecordResponse> addMedicalRecord(@PathVariable Long patientId,
                                                                    @Valid @RequestBody AddRecordRequest requestBody,
                                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        MedicalRecord record = medicalRecordService.addMedicalRecord(
                patientId, currentUser.getId(), requestBody.diagnosis(), requestBody.medicineName(),
                requestBody.dosage(), requestBody.frequency(), requestBody.duration(),
                requestBody.instructions(), requestBody.visitNotes());
        accessService.recordAccess(currentUser.getId(), patientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicalRecordResponse.from(record));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<MedicalRecord> myRecords = medicalRecordService.getRecordsByDoctor(currentUser.getId());
        List<MedicalRecordResponse> recentConsultations = myRecords.stream()
                .limit(5).map(MedicalRecordResponse::from).toList();
        return Map.of(
                "todaysPatients", accessService.getTodayCount(currentUser.getId()),
                "recentConsultations", recentConsultations
        );
    }
}
