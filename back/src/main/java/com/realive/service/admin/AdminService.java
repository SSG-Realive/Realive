package com.realive.service.admin;

import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.dto.admin.AdminReadDTO;

import java.util.Optional;

public interface AdminService {

    AdminLoginResponseDTO login(AdminLoginRequestDTO loginRequestDto);

    Optional<AdminReadDTO> getAdminById(Integer adminId);

    Optional<AdminReadDTO> getAdminByEmail(String email);

}
