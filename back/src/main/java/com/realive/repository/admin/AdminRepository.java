package com.realive.repository.admin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.admin.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer>{

    Optional<Admin> findByEmail(String email);
}