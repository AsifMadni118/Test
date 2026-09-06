package com.proteinpro.profile.web;

import com.proteinpro.profile.dto.ProfileDtos.ProfileResponse;
import com.proteinpro.profile.dto.ProfileDtos.RegistrationRequest;
import com.proteinpro.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.proteinpro.profile.service.UserProfileService;
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
public class UserProfileController {
    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody RegistrationRequest request) {
        return service.register(request);
    }

    @GetMapping("/me")
    public ProfileResponse getMyProfile(@RequestAttribute("authenticatedUserId") String userId) {
        return service.getById(userId);
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(@RequestAttribute("authenticatedUserId") String userId,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        return service.update(userId, request);
    }
}
