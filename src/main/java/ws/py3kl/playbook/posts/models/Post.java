package ws.py3kl.playbook.posts.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ws.py3kl.playbook.posts.exceptions.PostNotFoundException;
import ws.py3kl.playbook.posts.exceptions.PostOwnershipException;
import ws.py3kl.playbook.posts.models.requests.UpdatePostRequest;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;
import java.util.Objects;

import static ws.py3kl.playbook.posts.exceptions.PostFaultOperationException.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_user_id", columnList = "user_id"),
    @Index(name = "idx_posts_parent_id", columnList = "parent_id"),
    @Index(name = "idx_posts_group_id", columnList = "group_id")
})
public class Post {

    @Id
    @GeneratedValue(generator = "posts_id_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "request_hash")
    private String requestHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @JsonIgnore
    private LocalDateTime deletedAt;

    @JsonIgnore
    public Post checkOwnership(User currentUser) {
        if (currentUser.isAdmin()) {
            return this;
        }
        if (!this.userId.equals(userId)) {
            throw new PostOwnershipException();
        }
        return this;
    }

    @JsonIgnore
    public Post checkAvailability() {
        if (this.deletedAt != null) {
            throw new PostNotFoundException();
        }
        return this;
    }

    @JsonIgnore
    public Post checkCompatibility(User currentUser, UpdatePostRequest request) {

        if (currentUser.isAdmin()) {
            return this;
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(getId())) {
                throw parentIdCannotBeEqualsPostId();
            }
            if (getParentId() != null && !getParentId().equals(request.getParentId())) {
                throw parentChangeIsNotAllowed();
            }
        }

        if (getGroupId() != null) {
            if (request.getGroupId() != null && !getGroupId().equals(request.getGroupId())) {
                throw groupChangeIsNotAllowed();
            }
        }

        return this;
    }

    public Post checkCreationRules(User currentUser) {
        if (currentUser.isAdmin()) {
            return this;
        }
        if(createdAt != null && Objects.equals(userId, currentUser.getId())) {
            var diffMinutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
            if (diffMinutes < 1) {
                throw duplicateRequestUnder(1);
            }
        }
        return this;
    }

    @JsonIgnore
    public boolean isPersonalPost() {
        return !isGroupPost() && !isAnswerPost();
    }

    @JsonIgnore
    public boolean isGroupPost() {
        return groupId != null;
    }

    @JsonIgnore
    public boolean isGroupTopLevelPost() {
        return isGroupPost() && parentId == null;
    }

    @JsonIgnore
    public boolean isAnswerPost() {
        return parentId != null;
    }
}

