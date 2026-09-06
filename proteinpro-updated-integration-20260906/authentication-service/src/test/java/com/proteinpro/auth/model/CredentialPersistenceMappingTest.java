package com.proteinpro.auth.model;

import com.proteinpro.auth.repository.CredentialRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialPersistenceMappingTest {
    @Test
    void usesJpaWithTheExistingStringIdentityContract() {
        Credential credential = new Credential(
                "1eaa3f3f-2f42-4b91-88eb-b2aaf438ac46",
                "learner@example.com",
                "$2a$10$1234567890123456789012345678901234567890123456789012");

        assertThat(Credential.class).hasAnnotation(Entity.class);
        assertThat(Credential.class.getAnnotation(Table.class).name()).isEqualTo("credentials");
        assertThat(JpaRepository.class).isAssignableFrom(CredentialRepository.class);
        assertThat(credential.getId()).hasSize(36);
        assertThat(credential.getUserId()).isEqualTo("1eaa3f3f-2f42-4b91-88eb-b2aaf438ac46");
        assertThat(credential.getEmail()).isEqualTo("learner@example.com");
        assertThat(credential.getPasswordHash()).isNotBlank();
        assertThat(credential.isActive()).isFalse();
        assertThat(credential.getCreatedAt()).isNotNull();
        assertThat(credential.getUpdatedAt()).isNotNull();

        credential.setActive(true);
        credential.setPasswordHash("new-hash");
        var updated = java.time.Instant.now();
        credential.setUpdatedAt(updated);
        assertThat(credential.isActive()).isTrue();
        assertThat(credential.getPasswordHash()).isEqualTo("new-hash");
        assertThat(credential.getUpdatedAt()).isEqualTo(updated);

        Credential empty = new Credential();
        empty.initializePersistenceFields();
        assertThat(empty.getId()).hasSize(36);
        assertThat(empty.getCreatedAt()).isNotNull();
        assertThat(empty.getUpdatedAt()).isEqualTo(empty.getCreatedAt());
        empty.initializePersistenceFields();
    }
}
