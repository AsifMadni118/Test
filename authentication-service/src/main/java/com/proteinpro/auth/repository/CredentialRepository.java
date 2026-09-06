package com.proteinpro.auth.repository;

import com.proteinpro.auth.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByEmail(String email);
    Optional<Credential> findByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
}
