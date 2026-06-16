package nl.codeclan.cvwiz.config;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsValidationExceptionsToBadRequest() throws Exception {
        assertError(handler.handleMethodArgumentNotValid(null), HttpStatus.BAD_REQUEST, "Request validation failed.");
        assertError(handler.handleConstraintViolation(new ConstraintViolationException("invalid", Set.of())), HttpStatus.BAD_REQUEST, "Request validation failed.");
        assertError(handler.handleUnreadableMessage(new HttpMessageNotReadableException("bad", null)), HttpStatus.BAD_REQUEST, "Invalid request body.");
        assertError(handler.handleIllegalArgument(new IllegalArgumentException("bad")), HttpStatus.BAD_REQUEST, "Invalid request.");
    }

    @Test
    void mapsAuthenticationAuthorizationNotFoundAndGenericExceptions() throws Exception {
        assertError(handler.handleAuthentication(new AuthenticationException("bad") {
        }), HttpStatus.UNAUTHORIZED, "Authentication failed.");
        assertError(handler.handleAccessDenied(new AccessDeniedException("denied")), HttpStatus.FORBIDDEN, "Access denied.");
        assertError(handler.handleNotFound(new FileNotFoundException("missing")), HttpStatus.NOT_FOUND, "Resource not found.");
        assertError(handler.handleGeneric(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
    }

    @Test
    void preservesResponseStatusAndUsesFallbackReasonWhenMissing() throws Exception {
        assertError(handler.handleResponseStatus(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many")), HttpStatus.TOO_MANY_REQUESTS, "Too many");
        assertError(handler.handleResponseStatus(new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT)), HttpStatus.I_AM_A_TEAPOT, "Request failed.");
    }

    private void assertError(ResponseEntity<?> response, HttpStatus status, String message) throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(status);
        Object body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(readRecordComponent(body, "status")).isEqualTo(status.value());
        assertThat(readRecordComponent(body, "message")).isEqualTo(message);
    }

    private Object readRecordComponent(Object body, String name) throws Exception {
        Method method = body.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(body);
    }
}
