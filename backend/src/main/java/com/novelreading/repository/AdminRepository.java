package com.novelreading.repository;

import com.novelreading.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 管理员数据访问
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findById(Long id);
}
