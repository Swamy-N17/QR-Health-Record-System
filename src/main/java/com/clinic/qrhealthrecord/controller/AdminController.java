package com.clinic.qrhealthrecord.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.qrhealthrecord.dto.AdminUpdateDoctorRequest;
import com.clinic.qrhealthrecord.dto.AdminUpdatePatientRequest;
import com.clinic.qrhealthrecord.dto.CreateDoctorRequest;
import com.clinic.qrhealthrecord.dto.CreatePatientRequest;
import com.clinic.qrhealthrecord.dto.DoctorResponse;
import com.clinic.qrhealthrecord.dto.PageResponse;
import com.clinic.qrhealthrecord.dto.PatientResponse;
import com.clinic.qrhealthrecord.dto.StatusUpdateRequest;
import com.clinic.qrhealthrecord.entity.Admin;
import com.clinic.qrhealthrecord.entity.Doctor;
import com.clinic.qrhealthrecord.entity.Patient;
import com.clinic.qrhealthrecord.security.CustomUserDetails;
import com.clinic.qrhealthrecord.service.AdminService;
import com.clinic.qrhealthrecord.service.DoctorService;
import com.clinic.qrhealthrecord.service.PatientService;
import com.clinic.qrhealthrecord.util.HealthCardGenerator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final HealthCardGenerator healthCardGenerator;

    public AdminController(AdminService adminService, DoctorService doctorService, PatientService patientService,
                           HealthCardGenerator healthCardGenerator) {
        this.adminService = adminService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.healthCardGenerator = healthCardGenerator;
    }

    // ---- Doctor Management ----

    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> registerDoctor(@Valid @RequestBody CreateDoctorRequest requestBody,
                                                           @AuthenticationPrincipal CustomUserDetails currentUser) {

        Admin admin = adminService.getAdminById(currentUser.getId());

        Doctor doctor = doctorService.registerDoctor(
                requestBody.fullName(), requestBody.email(), requestBody.password(), requestBody.specialization(),
                requestBody.phoneNumber(), requestBody.dateOfBirth(), requestBody.gender(), requestBody.address(),
                admin
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(DoctorResponse.from(doctor));
    }

    @GetMapping("/doctors")
    public PageResponse<DoctorResponse> getAllDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Doctor> filtered = doctorService.getAllDoctors().stream()
                .filter(d -> search == null || search.isBlank()
                        || d.getFullName().toLowerCase().contains(search.toLowerCase())
                        || d.getEmail().toLowerCase().contains(search.toLowerCase())
                        || (d.getDoctorCode() != null && d.getDoctorCode().toLowerCase().contains(search.toLowerCase())))
                .sorted(Comparator.comparing(Doctor::getId).reversed())
                .toList();

        return paginate(filtered, page, size, DoctorResponse::from);
    }

    @PutMapping("/doctors/{id}")
    public DoctorResponse updateDoctor(@PathVariable Long id, @Valid @RequestBody AdminUpdateDoctorRequest requestBody) {
        Doctor doctor = doctorService.updateDoctorByAdmin(id, requestBody.fullName(), requestBody.email(),
                requestBody.phoneNumber(), requestBody.address(), requestBody.specialization());
        return DoctorResponse.from(doctor);
    }

    @PutMapping("/doctors/{id}/status")
    public DoctorResponse updateDoctorStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest requestBody) {
        return DoctorResponse.from(doctorService.updateStatus(id, requestBody.active()));
    }

    // ---- Patient Management ----

    @PostMapping("/patients")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody CreatePatientRequest requestBody,
                                                             @AuthenticationPrincipal CustomUserDetails currentUser) {

        Admin admin = adminService.getAdminById(currentUser.getId());

        Patient patient = patientService.registerPatient(
                requestBody.fullName(), requestBody.email(), requestBody.password(), requestBody.phoneNumber(),
                requestBody.age(), requestBody.gender(), requestBody.address(), requestBody.dateOfBirth(),
                requestBody.bloodGroup(), requestBody.emergencyContact(), admin
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientResponse.from(patient));
    }

    @GetMapping("/patients/{id}/health-card")
    public ResponseEntity<byte[]> getPatientHealthCard(@PathVariable Long id) throws Exception {
        Patient patient = patientService.getPatientById(id);
        byte[] cardBytes = healthCardGenerator.generateHealthCard(patient);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + patient.getPatientCode() + "-health-card.png\"")
                .body(cardBytes);
    }

    @GetMapping("/patients")
    public PageResponse<PatientResponse> getAllPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Patient> filtered = patientService.getAllPatients().stream()
                .filter(p -> search == null || search.isBlank()
                        || p.getFullName().toLowerCase().contains(search.toLowerCase())
                        || p.getEmail().toLowerCase().contains(search.toLowerCase())
                        || (p.getPatientCode() != null && p.getPatientCode().toLowerCase().contains(search.toLowerCase())))
                .sorted(Comparator.comparing(Patient::getId).reversed())
                .toList();

        return paginate(filtered, page, size, PatientResponse::from);
    }

    @PutMapping("/patients/{id}")
    public PatientResponse updatePatient(@PathVariable Long id, @Valid @RequestBody AdminUpdatePatientRequest requestBody) {
        Patient patient = patientService.updatePatientByAdmin(id, requestBody.fullName(), requestBody.email(),
                requestBody.phoneNumber(), requestBody.dateOfBirth(), requestBody.bloodGroup(),
                requestBody.address(), requestBody.emergencyContact());
        return PatientResponse.from(patient);
    }

    @PutMapping("/patients/{id}/status")
    public PatientResponse updatePatientStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest requestBody) {
        return PatientResponse.from(patientService.updateStatus(id, requestBody.active()));
    }

    // ---- Shared pagination helper ----

    private <E, R> PageResponse<R> paginate(List<E> filtered, int page, int size, Function<E, R> mapper) {
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<R> pageContent = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .map(mapper)
                .toList();

        return new PageResponse<>(pageContent, page, totalPages, totalElements);
    }

    // ---- Dashboard ----

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {

        List<Doctor> allDoctors = doctorService.getAllDoctors();
        List<Patient> allPatients = patientService.getAllPatients();

        List<DoctorResponse> recentDoctors = allDoctors.stream()
                .sorted(Comparator.comparing(Doctor::getId).reversed())
                .limit(5)
                .map(DoctorResponse::from)
                .toList();

        List<PatientResponse> recentPatients = allPatients.stream()
                .sorted(Comparator.comparing(Patient::getId).reversed())
                .limit(5)
                .map(PatientResponse::from)
                .toList();

        return Map.of(
                "totalDoctors", allDoctors.size(),
                "totalPatients", allPatients.size(),
                "recentDoctors", recentDoctors,
                "recentPatients", recentPatients
        );
    }
}