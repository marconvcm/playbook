package ws.py3kl.playbook.groups.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GroupNotFoundException extends ResponseStatusException {

    public GroupNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Group not found");
    }
}


