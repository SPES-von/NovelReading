package com.novelreading.repository;

import com.novelreading.entity.UserReadPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReadPreferencesRepository extends JpaRepository<UserReadPreferences, Long> {

    Optional<UserReadPreferences> findByUser_InternalId(Long userId);
}
