package com.realive.service.admin;

import com.realive.dto.admin.*;
import java.util.Optional;

public interface AdminService {

    AdminLoginResponseDTO login(AdminLoginRequestDTO loginRequestDto);

    AdminLoginResponseDTO getMyInfo(Integer adminId);

    Optional<AdminReadDTO> getAdminById(Integer adminId);

    Optional<AdminReadDTO> getAdminByEmail(String email);


}
