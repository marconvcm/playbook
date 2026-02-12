package ws.py3kl.playbook.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String[] errors;
    private String message;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message, String ...error) {
        ErrorResponse response = new ErrorResponse();
        response.setErrors(error);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
