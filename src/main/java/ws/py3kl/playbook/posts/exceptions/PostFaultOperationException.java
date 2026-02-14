package ws.py3kl.playbook.posts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PostFaultOperationException extends ResponseStatusException {

    public PostFaultOperationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
    
    public static PostFaultOperationException groupChangeIsNotAllowed() {
        return new PostFaultOperationException("Changing the group of a post is not allowed");
    }

    public static PostFaultOperationException parentChangeIsNotAllowed() {
        return new PostFaultOperationException("Changing the parent of a post is not allowed");
    }

    public static PostFaultOperationException parentIdCannotBeEqualsPostId() {
        return new PostFaultOperationException("The parent ID cannot be the same as the post ID");
    }

    public static PostFaultOperationException duplicateRequestUnder(int minutes) {
        return new PostFaultOperationException("Duplicate post creation request detected within the last " + minutes + " minute(s)");
    }
}
