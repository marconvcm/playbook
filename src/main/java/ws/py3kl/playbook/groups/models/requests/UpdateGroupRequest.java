package ws.py3kl.playbook.groups.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGroupRequest {

    @NotBlank
    private String title;

    private String summary;
}
