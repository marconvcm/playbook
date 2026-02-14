package ws.py3kl.playbook.posts.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ws.py3kl.playbook.posts.exceptions.PostNotFoundException;
import ws.py3kl.playbook.posts.models.Post;
import ws.py3kl.playbook.posts.models.requests.CreatePostRequest;
import ws.py3kl.playbook.posts.models.requests.UpdatePostRequest;
import ws.py3kl.playbook.posts.repositories.PostRepository;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static ws.py3kl.playbook.posts.exceptions.PostFaultOperationException.parentChangeIsNotAllowed;
import static ws.py3kl.playbook.posts.exceptions.PostFaultOperationException.parentIdCannotBeEqualsPostId;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAllByDeletedAtIsNull();
    }

    public List<Post> findAllByUserId(Long userId) {
        return postRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
            .stream()
            .filter(Post::isPersonalPost)
            .toList();
    }

    public List<Post> findAllByGroupId(Long groupId) {
        return postRepository.findAllByGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(groupId)
            .stream()
            .filter(Post::isGroupTopLevelPost)
            .toList();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public Post create(User currentUser, CreatePostRequest createPostRequest) {
        String requestHash = String.valueOf(createPostRequest.hashCode());
        LocalDateTime now = LocalDateTime.now();

        postRepository.findAllByRequestHash(requestHash)
            .forEach(post -> post.checkCreationRules(currentUser));

        Post post = new Post();
        post.setUserId(currentUser.getId());
        post.setParentId(createPostRequest.getParentId());
        post.setGroupId(createPostRequest.getGroupId());
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setRequestHash(requestHash);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, User currentUser, UpdatePostRequest request) {
        Post post = findById(id)
            .checkAvailability()
            .checkOwnership(currentUser)
            .checkCompatibility(currentUser, request);

        post.setParentId(request.getParentId());
        post.setGroupId(request.getGroupId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Post post = findById(id)
            .checkAvailability()
            .checkOwnership(currentUser);

        post.setDeletedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }
}
