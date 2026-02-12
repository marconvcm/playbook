package ws.py3kl.playbook.user.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.user.models.AuthRequest;
import ws.py3kl.playbook.user.models.AuthResponse;
import ws.py3kl.playbook.user.models.CreateUserRequest;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.services.UserService;
import ws.py3kl.playbook.utils.AcknowledgeResponse;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public AcknowledgeResponse<User> signUp(@Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        return AcknowledgeResponse.of(user, "User created successfully");
    }

    @PostMapping("/signin")
    public AcknowledgeResponse<AuthResponse> signIn(@Valid @RequestBody AuthRequest authRequest) {
        User user = userService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        return AcknowledgeResponse.of(new AuthResponse(
            user.getAccessToken(),
            user.getRefreshToken(),
            user.getTokenExpiresIn()
        ), "Authentication successful");
    }
}
