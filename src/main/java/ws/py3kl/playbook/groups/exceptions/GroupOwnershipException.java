package ws.py3kl.playbook.groups.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GroupOwnershipException extends ResponseStatusException {

    public GroupOwnershipException() {
        super(HttpStatus.FORBIDDEN, "You do not own this group");
    }
}


