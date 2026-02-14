package ws.py3kl.playbook.groups.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GroupFaultOperationException extends ResponseStatusException {

    public GroupFaultOperationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static GroupFaultOperationException groupIdAlreadyExists() {
        return new GroupFaultOperationException("Group ID already exists");
    }
}


