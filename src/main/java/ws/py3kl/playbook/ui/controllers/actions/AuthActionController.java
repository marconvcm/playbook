package ws.py3kl.playbook.ui.controllers.actions;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ws.py3kl.playbook.ui.models.SignInForm;
import ws.py3kl.playbook.ui.models.SignUpForm;
import ws.py3kl.playbook.ui.services.UiSessionService;
import ws.py3kl.playbook.user.models.CreateUserRequest;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.services.UserService;

@Controller
public class AuthActionController {

    @Autowired
    private UserService userService;

    @Autowired
    private UiSessionService uiSessionService;

    @PostMapping("/app/ui/signin")
    public String signIn(
        @Valid @ModelAttribute SignInForm signInForm,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email and password.");
            return "redirect:/app";
        }

        try {
            User user = userService.authenticate(signInForm.getEmail(), signInForm.getPassword());
            uiSessionService.signIn(session, user);
            redirectAttributes.addFlashAttribute("successMessage", "Signed in successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sign in failed.");
        }
        return "redirect:/app";
    }

    @PostMapping("/app/ui/signup")
    public String signUp(
        @Valid @ModelAttribute SignUpForm signUpForm,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all signup fields with valid values.");
            return "redirect:/app";
        }

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail(signUpForm.getEmail());
        createUserRequest.setHandle(signUpForm.getHandle());
        createUserRequest.setPassword(signUpForm.getPassword());
        createUserRequest.setFirstName(signUpForm.getFirstName());
        createUserRequest.setLastName(signUpForm.getLastName());

        try {
            userService.createUser(createUserRequest);
            User user = userService.authenticate(signUpForm.getEmail(), signUpForm.getPassword());
            uiSessionService.signIn(session, user);
            redirectAttributes.addFlashAttribute("successMessage", "Account created and signed in.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account creation failed.");
        }
        return "redirect:/app";
    }

    @PostMapping("/app/ui/signout")
    public String signOut(HttpSession session, RedirectAttributes redirectAttributes) {
        uiSessionService.signOut(session);
        redirectAttributes.addFlashAttribute("successMessage", "Signed out.");
        return "redirect:/app";
    }
}
