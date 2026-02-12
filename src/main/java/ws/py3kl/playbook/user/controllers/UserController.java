package ws.py3kl.playbook.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.services.UserService;
import ws.py3kl.playbook.user.utils.CurrentUser;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    public UserService userService;

    @GetMapping("/me")
    public User getCurrentUser(@CurrentUser User currentUser) {
        return currentUser;
    }
}
