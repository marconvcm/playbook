package ws.py3kl.playbook.groups.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups_members")
public class GroupMember {

    @Id
    @GeneratedValue(generator = "group_member_id_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private int groupId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Group group;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @Column(name = "is_admitted", nullable = false, columnDefinition = "boolean default true")
    private boolean isAdmitted = true;

    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;
}
