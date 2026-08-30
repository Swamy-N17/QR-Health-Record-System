package com.clinic.qrhealthrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Which Super Admin created this Admin
    @ManyToOne
    @JoinColumn(name = "created_by_super_admin_id", nullable = false)
    private SuperAdmin createdBy;

    // ---- Constructors ----

    public Admin() {
    }

    public Admin(String fullName, String email, String password, SuperAdmin createdBy) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.createdBy = createdBy;
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SuperAdmin getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SuperAdmin createdBy) {
        this.createdBy = createdBy;
    }
}