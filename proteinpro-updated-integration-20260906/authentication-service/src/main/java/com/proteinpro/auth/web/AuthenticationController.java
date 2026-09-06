package com.proteinpro.auth.web;

import com.proteinpro.auth.dto.AuthDtos.LoginRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginResponse;
import com.proteinpro.auth.dto.AuthDtos.PasswordResetRequest;
import com.proteinpro.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(
            @RequestAttribute("authenticatedUserId") String userId,
            @Valid @RequestBody PasswordResetRequest request) {
        authenticationService.resetPassword(userId, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
