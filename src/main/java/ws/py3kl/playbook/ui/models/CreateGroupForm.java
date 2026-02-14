package ws.py3kl.playbook.ui.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupForm {

    @NotBlank
    private String groupId;

    @NotBlank
    private String title;

    private String summary;
}
