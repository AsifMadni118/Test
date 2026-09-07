package com.proteinpro.auth.web;

import com.proteinpro.auth.dto.AuthDtos.LoginRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginResponse;
import com.proteinpro.auth.dto.AuthDtos.PasswordResetRequest;
import com.proteinpro.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, password reset, and session termination")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates user credentials and generates a signed JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public LoginResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true)
            @Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Reset password", description = "Resets the authenticated user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<Void> resetPassword(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New password payload", required = true)
            @Valid @RequestBody PasswordResetRequest request) {
        authenticationService.resetPassword(userId, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Terminates the user session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully logged out")
    })
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}

