package ws.py3kl.playbook.posts.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostRequest {

    private Long parentId;

    private Long groupId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
