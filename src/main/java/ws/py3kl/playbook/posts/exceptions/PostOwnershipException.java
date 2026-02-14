package ws.py3kl.playbook.posts.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class PostOwnershipException extends ResponseStatusException {

    public PostOwnershipException() {
        super(HttpStatus.BAD_REQUEST, "This post does not belong to you");
    }
}

