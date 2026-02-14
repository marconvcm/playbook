package ws.py3kl.playbook.ui.controllers.pages;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.ui.services.UiSessionService;

@Controller
public class PostPageController {

    @Autowired
    private UiSessionService uiSessionService;

    @Autowired
    private PostService postService;

    @GetMapping("/app/posts/{id}")
    public String postPage(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("currentUser", uiSessionService.getCurrentUser(session).orElse(null));
        model.addAttribute("topic", postService.findById(id));
        model.addAttribute("answers", postService.findReplies(id));
        return "post-page";
    }
}
