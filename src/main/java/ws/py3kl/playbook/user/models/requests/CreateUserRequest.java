package ws.py3kl.playbook.user.models.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateUserRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Handle must be alphanumeric with underscores only")
    private String handle;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Length(min = 8, max = 100)
    private String password;
}
