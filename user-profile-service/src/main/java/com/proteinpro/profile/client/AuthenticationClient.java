package com.proteinpro.profile.client;

import com.proteinpro.profile.dto.ProfileDtos.CreateCredentialRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service")
public interface AuthenticationClient {
    @PostMapping("/internal/credentials")
    ResponseEntity<Void> createCredential(
            @RequestHeader("X-Internal-Api-Key") String internalApiKey,
            @RequestBody CreateCredentialRequest request);
}
