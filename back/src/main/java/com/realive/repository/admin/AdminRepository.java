package com.realive.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.admin.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long>{
}
