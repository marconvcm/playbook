package ws.py3kl.playbook.ui.controllers.actions;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ws.py3kl.playbook.groups.models.requests.CreateGroupRequest;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.posts.models.requests.CreatePostRequest;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.ui.models.CreateGroupForm;
import ws.py3kl.playbook.ui.models.CreatePostForm;
import ws.py3kl.playbook.ui.services.UiSessionService;
import ws.py3kl.playbook.user.models.User;

import java.util.Optional;

@Controller
public class ContentActionController {

    @Autowired
    private UiSessionService uiSessionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    @PostMapping("/app/ui/groups")
    public String createGroup(
        @Valid @ModelAttribute CreateGroupForm createGroupForm,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        Optional<User> currentUser = uiSessionService.getCurrentUser(session);

        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sign in first to create a group.");
            return "redirect:/app";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Group ID and title are required.");
            return "redirect:/app";
        }

        CreateGroupRequest createGroupRequest = new CreateGroupRequest();
        createGroupRequest.setGroupId(createGroupForm.getGroupId());
        createGroupRequest.setTitle(createGroupForm.getTitle());
        createGroupRequest.setSummary(createGroupForm.getSummary());

        try {
            groupService.create(currentUser.get(), createGroupRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Group created.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to create group.");
        }
        return "redirect:/app";
    }

    @PostMapping("/app/ui/posts")
    public String createPost(
        @Valid @ModelAttribute CreatePostForm createPostForm,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        Optional<User> currentUser = uiSessionService.getCurrentUser(session);

        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sign in first to create a post.");
            return "redirect:/app";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Post title and content are required.");
            return "redirect:/app";
        }

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setParentId(createPostForm.getParentId());
        createPostRequest.setGroupId(createPostForm.getGroupId());
        createPostRequest.setTitle(createPostForm.getTitle());
        createPostRequest.setContent(createPostForm.getContent());

        try {
            postService.create(currentUser.get(), createPostRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Post published.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to publish post.");
        }
        return "redirect:/app";
    }
}
