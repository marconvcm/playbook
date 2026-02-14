package ws.py3kl.playbook.ui.controllers.pages;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.ui.models.CreateGroupForm;
import ws.py3kl.playbook.ui.models.CreatePostForm;
import ws.py3kl.playbook.ui.models.SignInForm;
import ws.py3kl.playbook.ui.models.SignUpForm;
import ws.py3kl.playbook.ui.services.UiSessionService;

@Controller
public class HomePageController {

    @Autowired
    private UiSessionService uiSessionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    @GetMapping("/app")
    public String home(Model model, HttpSession session) {
        model.addAttribute("currentUser", uiSessionService.getCurrentUser(session).orElse(null));
        model.addAttribute("groups", groupService.findAll());
        model.addAttribute("posts", postService.findAll());

        model.addAttribute("signInForm", new SignInForm());
        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute("createGroupForm", new CreateGroupForm());
        model.addAttribute("createPostForm", new CreatePostForm());
        return "home";
    }
}
