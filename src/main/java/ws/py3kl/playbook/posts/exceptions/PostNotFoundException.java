package ws.py3kl.playbook.posts.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class PostNotFoundException extends ResponseStatusException {

    public PostNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Post not found");
    }
}

