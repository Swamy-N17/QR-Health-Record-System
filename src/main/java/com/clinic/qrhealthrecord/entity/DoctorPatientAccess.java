package com.clinic.qrhealthrecord.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "doctor_patient_access", uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_patient_day", columnNames = {"doctor_id", "patient_id", "access_date"})
})
public class DoctorPatientAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    public DoctorPatientAccess() {}

    public DoctorPatientAccess(Doctor doctor, Patient patient, LocalDate accessDate) {
        this.doctor = doctor;
        this.patient = patient;
        this.accessDate = accessDate;
    }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public LocalDate getAccessDate() { return accessDate; }
}
