package com.proteinpro.profile.web;

import com.proteinpro.profile.dto.ProfileDtos.ProfileResponse;
import com.proteinpro.profile.dto.ProfileDtos.RegistrationRequest;
import com.proteinpro.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.proteinpro.profile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@Tag(name = "User Profile", description = "Endpoints for user registration and profile management")
public class UserProfileController {
    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user", description = "Creates a new user profile and provisions authentication credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "User email already exists")
    })
    public ProfileResponse register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration details", required = true)
            @Valid @RequestBody RegistrationRequest request) {
        return service.register(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves profile details for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ProfileResponse getMyProfile(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId) {
        return service.getById(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates profile details for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid profile update data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ProfileResponse updateMyProfile(
            @Parameter(hidden = true)
            @RequestAttribute("authenticatedUserId") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated profile fields", required = true)
            @Valid @RequestBody UpdateProfileRequest request) {
        return service.update(userId, request);
    }
}

