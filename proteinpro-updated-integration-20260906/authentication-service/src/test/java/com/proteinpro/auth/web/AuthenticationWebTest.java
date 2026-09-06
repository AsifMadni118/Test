package com.proteinpro.auth.web;

import com.proteinpro.auth.dto.AuthDtos.CreateCredentialRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginResponse;
import com.proteinpro.auth.dto.AuthDtos.PasswordResetRequest;
import com.proteinpro.auth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationWebTest {
    @Test
    void controllersDelegateAndReturnIntentionalStatuses() {
        AuthenticationService service = mock(AuthenticationService.class);
        AuthenticationController controller = new AuthenticationController(service);
        LoginRequest login = new LoginRequest("learner@example.com", "password123");
        LoginResponse response = new LoginResponse("token", "Bearer", 900);
        when(service.login(login)).thenReturn(response);
        assertThat(controller.login(login)).isEqualTo(response);

        PasswordResetRequest reset = new PasswordResetRequest("newpassword");
        assertThat(controller.resetPassword("user-1", reset).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).resetPassword("user-1", "newpassword");
        assertThat(controller.logout().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        InternalCredentialController internal = new InternalCredentialController(service, "internal-secret");
        CreateCredentialRequest create = new CreateCredentialRequest("user-1", "learner@example.com", "password123");
        assertThat(internal.create("internal-secret", create).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(service).createCredential(create);
        assertThatThrownBy(() -> internal.create("wrong-secret", create))
                .isInstanceOf(ApiException.class).hasMessage("Internal service authentication failed");
    }

    @Test
    void exceptionHandlerReturnsStableTypedErrorContract() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        var api = handler.handleApi(new ApiException(HttpStatus.NOT_FOUND, "missing"), request);
        assertThat(api.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(api.getBody().message()).isEqualTo("missing");
        assertThat(api.getBody().timestamp()).isNotBlank();
        assertThat(api.getBody().error()).isEqualTo("Not Found");
        assertThat(api.getBody().path()).isEqualTo("/api/auth/login");

        BindingResult binding = mock(BindingResult.class);
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("request", "email", "must be valid")));
        MethodArgumentNotValidException invalid = mock(MethodArgumentNotValidException.class);
        when(invalid.getBindingResult()).thenReturn(binding);
        assertThat(handler.handleValidation(invalid, request).getBody().message()).isEqualTo("email: must be valid");

        when(binding.getFieldErrors()).thenReturn(List.of());
        assertThat(handler.handleValidation(invalid, request).getBody().message()).isEqualTo("Request validation failed");
        assertThat(handler.handleMalformedJson(new HttpMessageNotReadableException("bad"), request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleDuplicate(new DataIntegrityViolationException("duplicate"), request).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.handleUnexpected(new RuntimeException("boom"), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
