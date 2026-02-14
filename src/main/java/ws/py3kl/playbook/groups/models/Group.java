package ws.py3kl.playbook.groups.models;

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
import ws.py3kl.playbook.groups.exceptions.GroupNotFoundException;
import ws.py3kl.playbook.groups.exceptions.GroupOwnershipException;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups", indexes = {
    @Index(name = "idx_groups_group_id", columnList = "group_id"),
    @Index(name = "idx_groups_owner_id", columnList = "owner_id")
})
public class Group {

    @Id
    @GeneratedValue(generator = "groups_id_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "group_id", nullable = false, unique = true)
    private String groupId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary")
    private String summary;

    @Column(name = "is_private", nullable = false, columnDefinition = "boolean default false")
    private Boolean isPrivate = false;

    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @JsonIgnore
    private LocalDateTime deletedAt;

    @JsonIgnore
    public Group checkOwnership(User currentUser) {
        if (currentUser.isAdmin()) {
            return this;
        }
        if (!this.ownerId.equals(currentUser.getId())) {
            throw new GroupOwnershipException();
        }
        return this;
    }

    @JsonIgnore
    public Group checkAvailability() {
        if (this.deletedAt != null) {
            throw new GroupNotFoundException();
        }
        return this;
    }

    public String getHref() {
        return "/v1/groups/" + this.id;
    }

    public String getJoinHref() {
        return getHref() + "/join";
    }

    public String getPostsHref() {
        return getHref() + "/posts";
    }
}
