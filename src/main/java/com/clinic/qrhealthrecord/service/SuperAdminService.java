package com.clinic.qrhealthrecord.service;

import org.springframework.stereotype.Service;

import com.clinic.qrhealthrecord.entity.SuperAdmin;
import com.clinic.qrhealthrecord.exception.ResourceNotFoundException;
import com.clinic.qrhealthrecord.repository.SuperAdminRepository;

@Service
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;

    public SuperAdminService(SuperAdminRepository superAdminRepository) {
        this.superAdminRepository = superAdminRepository;
    }

    public SuperAdmin getSuperAdminById(Long id) {
        return superAdminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SuperAdmin not found with id: " + id));
    }
}