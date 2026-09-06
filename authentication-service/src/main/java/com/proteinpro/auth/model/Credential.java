package com.proteinpro.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credentials", uniqueConstraints = {
        @UniqueConstraint(name = "uk_credentials_user_id", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_credentials_email", columnNames = "email")
})
public class Credential {
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, updatable = false, length = 36)
    private String userId;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Credential() {
    }

    public Credential(String userId, String email, String passwordHash) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = false;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PrePersist
    void initializePersistenceFields() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active) { this.active = active; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
