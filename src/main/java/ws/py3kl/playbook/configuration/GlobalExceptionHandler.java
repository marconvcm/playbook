package ws.py3kl.playbook.configuration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ws.py3kl.playbook.user.exceptions.WrongPasswordException;
import ws.py3kl.playbook.utils.ErrorResponse;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
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
