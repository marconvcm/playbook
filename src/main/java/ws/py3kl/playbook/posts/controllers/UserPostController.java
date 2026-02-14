package ws.py3kl.playbook.posts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.posts.models.Post;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.utils.CurrentUser;
import ws.py3kl.playbook.utils.AcknowledgeResponse;

import java.util.List;

@RestController
public class UserPostController {

    @Autowired
    private PostService postService;

    @GetMapping("/v1/users/me/posts")
    public AcknowledgeResponse<List<Post>> getPosts(@CurrentUser User user) {
        return AcknowledgeResponse.of(postService.findAllByUserId(user.getId()), "Posts fetched successfully");
    }

    @GetMapping("/v1/users/{id}/posts")
    public AcknowledgeResponse<List<Post>> getPosts(@PathVariable("id") Long id) {
        return AcknowledgeResponse.of(postService.findAllByUserId(id), "Posts fetched successfully");
    }
}
