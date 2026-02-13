package ws.py3kl.playbook.posts.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ws.py3kl.playbook.posts.models.Post;
import ws.py3kl.playbook.posts.models.requests.CreatePostRequest;
import ws.py3kl.playbook.posts.models.requests.UpdatePostRequest;
import ws.py3kl.playbook.posts.repositories.PostRepository;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll()
            .stream()
            .filter(post -> post.getDeletedAt() == null)
            .toList();
    }

    public Post findById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        return post;
    }

    @Transactional
    public Post create(User currentUser, CreatePostRequest createPostRequest) {
        LocalDateTime now = LocalDateTime.now();

        Post post = new Post();
        post.setUserId(currentUser.getId());
        post.setParentId(createPostRequest.getParentId());
        post.setGroupId(createPostRequest.getGroupId());
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, User currentUser, UpdatePostRequest updatePostRequest) {
        Post post = findById(id);

        if (!post.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this post");
        }

        post.setParentId(updatePostRequest.getParentId());
        post.setGroupId(updatePostRequest.getGroupId());
        post.setTitle(updatePostRequest.getTitle());
        post.setContent(updatePostRequest.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Post post = findById(id);

        if (!post.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this post");
        }

        post.setDeletedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }
}
