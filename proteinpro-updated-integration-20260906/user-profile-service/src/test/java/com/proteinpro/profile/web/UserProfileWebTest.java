package com.proteinpro.profile.web;

import com.proteinpro.profile.dto.ProfileDtos.ProfileResponse;
import com.proteinpro.profile.dto.ProfileDtos.RegistrationRequest;
import com.proteinpro.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.proteinpro.profile.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileWebTest {
    @Test
    void controllerDelegatesAllProfileOperations() {
        UserProfileService service = mock(UserProfileService.class);
        UserProfileController controller = new UserProfileController(service);
        RegistrationRequest registration = new RegistrationRequest("Ada", "Lovelace", "ada@example.com", "password123");
        UpdateProfileRequest update = new UpdateProfileRequest("Grace", "Hopper");
        ProfileResponse response = new ProfileResponse("user-1", "Ada", "Lovelace", "ada@example.com",
                Instant.now(), Instant.now());
        when(service.register(registration)).thenReturn(response);
        when(service.getById("user-1")).thenReturn(response);
        when(service.update("user-1", update)).thenReturn(response);
        assertThat(controller.register(registration)).isEqualTo(response);
        assertThat(controller.getMyProfile("user-1")).isEqualTo(response);
        assertThat(controller.updateMyProfile("user-1", update)).isEqualTo(response);
        verify(service).update("user-1", update);
    }

    @Test
    void handlerMapsExpectedAndUnexpectedFailuresToTypedErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/profiles/me");
        var api = handler.api(new ApiException(HttpStatus.NOT_FOUND, "missing"), request);
        assertThat(api.getBody().message()).isEqualTo("missing");
        assertThat(api.getBody().status()).isEqualTo(404);
        assertThat(api.getBody().error()).isEqualTo("Not Found");
        assertThat(api.getBody().timestamp()).isNotBlank();
        assertThat(api.getBody().path()).isEqualTo("/api/profiles/me");

        BindingResult binding = mock(BindingResult.class);
        MethodArgumentNotValidException invalid = mock(MethodArgumentNotValidException.class);
        when(invalid.getBindingResult()).thenReturn(binding);
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("request", "firstName", "must not be blank")));
        assertThat(handler.validation(invalid, request).getBody().message()).contains("firstName");
        when(binding.getFieldErrors()).thenReturn(List.of());
        assertThat(handler.validation(invalid, request).getBody().message()).isEqualTo("Request validation failed");
        assertThat(handler.malformedJson(new HttpMessageNotReadableException("bad"), request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.duplicate(new DataIntegrityViolationException("duplicate"), request).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.unexpected(new RuntimeException("boom"), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
