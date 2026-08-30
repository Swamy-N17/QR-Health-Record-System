package com.clinic.qrhealthrecord.dto;

import com.clinic.qrhealthrecord.entity.Admin;

public record AdminResponse(Long id, String fullName, String email) {

    public static AdminResponse from(Admin admin) {
        return new AdminResponse(admin.getId(), admin.getFullName(), admin.getEmail());
    }
}