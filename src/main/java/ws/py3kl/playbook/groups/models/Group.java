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

    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @JsonIgnore
    private LocalDateTime deletedAt;
}
