package ws.py3kl.playbook.posts.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ws.py3kl.playbook.posts.models.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    List<Post> findByGroupId(Long groupId);
}
