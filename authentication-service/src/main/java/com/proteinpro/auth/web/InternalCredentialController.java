package com.proteinpro.auth.web;

import com.proteinpro.auth.dto.AuthDtos.CreateCredentialRequest;
import com.proteinpro.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Internal Credentials", description = "Internal service-to-service credential provisioning")
public class InternalCredentialController {
    private final AuthenticationService authenticationService;
    private final byte[] internalApiKey;

    public InternalCredentialController(AuthenticationService authenticationService,
                                        @Value("${security.internal-api-key}") String internalApiKey) {
        this.authenticationService = authenticationService;
        this.internalApiKey = internalApiKey.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    @Operation(summary = "Provision credential", description = "Internal endpoint called during user registration to store encrypted password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Credential successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid credential payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - invalid internal API key"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<Void> create(
            @Parameter(description = "Internal service authentication secret key", required = true)
            @RequestHeader("X-Internal-Api-Key") String suppliedKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credential creation payload", required = true)
            @Valid @RequestBody CreateCredentialRequest request) {
        if (!MessageDigest.isEqual(internalApiKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Internal service authentication failed");
        }
        authenticationService.createCredential(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

