package ws.py3kl.playbook.ui.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.services.UserService;

import java.util.Optional;

@Service
public class UiSessionService {

    private static final String ACCESS_TOKEN_SESSION_KEY = "playbook_access_token";

    @Autowired
    private UserService userService;

    public Optional<User> getCurrentUser(HttpSession session) {
        Object token = session.getAttribute(ACCESS_TOKEN_SESSION_KEY);
        if (token instanceof String value && !value.isBlank()) {
            return userService.findByAccessToken(value);
        }
        return Optional.empty();
    }

    public void signIn(HttpSession session, User user) {
        session.setAttribute(ACCESS_TOKEN_SESSION_KEY, user.getAccessToken());
    }

    public void signOut(HttpSession session) {
        session.removeAttribute(ACCESS_TOKEN_SESSION_KEY);
    }
}
