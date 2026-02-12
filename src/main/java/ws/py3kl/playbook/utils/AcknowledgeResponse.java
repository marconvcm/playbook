package ws.py3kl.playbook.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AcknowledgeResponse<T> {
    private T data;
    private String message;
    private LocalDateTime timestamp;

    public static <T> AcknowledgeResponse<T> of(T data, String message) {
        AcknowledgeResponse<T> response = new AcknowledgeResponse<>();
        response.setData(data);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
