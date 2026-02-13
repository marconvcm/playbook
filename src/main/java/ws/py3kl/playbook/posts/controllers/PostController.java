package ws.py3kl.playbook.posts.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.posts.models.Post;
import ws.py3kl.playbook.posts.models.requests.CreatePostRequest;
import ws.py3kl.playbook.posts.models.requests.UpdatePostRequest;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.utils.CurrentUser;
import ws.py3kl.playbook.utils.AcknowledgeResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public AcknowledgeResponse<List<Post>> getPosts() {
        return AcknowledgeResponse.of(postService.findAll(), "Posts fetched successfully");
    }

    @GetMapping("/{id}")
    public AcknowledgeResponse<Post> getPost(@PathVariable Long id) {
        return AcknowledgeResponse.of(postService.findById(id), "Post fetched successfully");
    }

    @PostMapping
    public AcknowledgeResponse<Post> createPost(@CurrentUser User currentUser, @Valid @RequestBody CreatePostRequest createPostRequest) {
        return AcknowledgeResponse.of(postService.create(currentUser, createPostRequest), "Post created successfully");
    }

    @PutMapping("/{id}")
    public AcknowledgeResponse<Post> updatePost(@PathVariable Long id, @CurrentUser User currentUser, @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        return AcknowledgeResponse.of(postService.update(id, currentUser, updatePostRequest), "Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public AcknowledgeResponse<Boolean> deletePost(@PathVariable Long id, @CurrentUser User currentUser) {
        postService.delete(id, currentUser);
        return AcknowledgeResponse.of(true, "Post deleted successfully");
    }
}
