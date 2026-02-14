package ws.py3kl.playbook.ui.controllers.pages;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.ui.services.UiSessionService;

@Controller
public class GroupPageController {

    @Autowired
    private UiSessionService uiSessionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    @GetMapping("/app/groups/{id}")
    public String groupProfile(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("currentUser", uiSessionService.getCurrentUser(session).orElse(null));
        model.addAttribute("group", groupService.findById(id));
        model.addAttribute("posts", postService.findByGroupId(id));
        return "group-profile";
    }
}
