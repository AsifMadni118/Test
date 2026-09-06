package com.proteinpro.profile.repository;

import com.proteinpro.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    boolean existsByEmail(String email);
}
