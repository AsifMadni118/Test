package com.proteinpro.auth.web;

import com.proteinpro.auth.dto.AuthDtos.CreateCredentialRequest;
import com.proteinpro.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/credentials")
public class InternalCredentialController {
    private final AuthenticationService authenticationService;
    private final byte[] internalApiKey;

    public InternalCredentialController(AuthenticationService authenticationService,
                                        @Value("${security.internal-api-key}") String internalApiKey) {
        this.authenticationService = authenticationService;
        this.internalApiKey = internalApiKey.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestHeader("X-Internal-Api-Key") String suppliedKey,
            @Valid @RequestBody CreateCredentialRequest request) {
        if (!MessageDigest.isEqual(internalApiKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Internal service authentication failed");
        }
        authenticationService.createCredential(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
