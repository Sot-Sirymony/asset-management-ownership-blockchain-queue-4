package com.up.asset_holder_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V3: Ensures global error handling returns consistent, measurable responses.
 */
class GlobalExceptionHandleTest {

    private final GlobalExceptionHandle handler = new GlobalExceptionHandle();

    @Test
    void handleNotFoundException_returns404WithDetail() {
        ProblemDetail result = handler.handleNotFoundExceptionCustom(new NotFoundException("Resource missing"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Resource missing");
        assertThat(result.getTitle()).isEqualTo("Not Found");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleIllegalStateException_returns503() {
        ProblemDetail result = handler.handleIllegalStateException(new IllegalStateException("Enrollment failed"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getTitle()).isEqualTo("Service Unavailable");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleAccessDenied_returns403() {
        ProblemDetail result = handler.handleAccessDeniedException(new AccessDeniedException("Denied"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("Access Denied");
    }

    @Test
    void handleBadCredentials_returns401() {
        ProblemDetail result = handler.handleBadCredentialsException(new BadCredentialsException("Bad"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getDetail()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleGenericException_returns500() {
        ProblemDetail result = handler.handleGenericException(new RuntimeException("Internal"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
    }
}
