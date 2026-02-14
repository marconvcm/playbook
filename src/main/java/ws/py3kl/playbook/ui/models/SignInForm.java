package ws.py3kl.playbook.ui.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
