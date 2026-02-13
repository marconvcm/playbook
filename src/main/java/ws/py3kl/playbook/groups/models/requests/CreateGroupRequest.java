package ws.py3kl.playbook.groups.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupRequest {

    @NotBlank
    private String groupId;

    @NotBlank
    private String title;

    private String summary;
}
