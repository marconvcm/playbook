package ws.py3kl.playbook.ui.controllers.pages;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ws.py3kl.playbook.ui.services.ProfilePageDataService;
import ws.py3kl.playbook.ui.services.UiSessionService;
import ws.py3kl.playbook.user.models.User;

@Controller
public class ProfilePageController {

    @Autowired
    private UiSessionService uiSessionService;

    @Autowired
    private ProfilePageDataService profilePageDataService;

    @GetMapping("/app/profile")
    public String profile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = uiSessionService.getCurrentUser(session).orElse(null);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sign in first to view your profile.");
            return "redirect:/app";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userPosts", profilePageDataService.findUserPosts(currentUser));
        model.addAttribute("ownedGroups", profilePageDataService.findOwnedGroups(currentUser));
        model.addAttribute("memberGroups", profilePageDataService.findMemberGroups(currentUser));
        return "profile";
    }
}
