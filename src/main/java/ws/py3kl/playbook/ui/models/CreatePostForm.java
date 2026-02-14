package ws.py3kl.playbook.ui.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostForm {

    private Long parentId;

    private Long groupId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
