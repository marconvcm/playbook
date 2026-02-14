package ws.py3kl.playbook.configuration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import ws.py3kl.playbook.user.exceptions.WrongPasswordException;
import ws.py3kl.playbook.utils.ErrorResponse;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(500)
            .body(ErrorResponse.of("An unexpected error occurred", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String[] errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .toArray(String[]::new);

        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("Validation error", errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String[] errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toArray(String[]::new);

        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("Validation error", errors));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("BAD REQUEST", ex.getReason()));
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWrongPasswordException(WrongPasswordException ex) {
        return ResponseEntity.status(401)
            .body(ErrorResponse.of("Invalid credentials"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(401)
            .body(ErrorResponse.of("Invalid credentials"));
    }
}
