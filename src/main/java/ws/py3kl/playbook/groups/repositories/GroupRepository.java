package ws.py3kl.playbook.groups.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ws.py3kl.playbook.groups.models.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupId(String groupId);

    boolean existsByGroupId(String groupId);

    List<Group> findAllByDeletedAtIsNull();

    List<Group> findAllByOwnerIdAndDeletedAtIsNull(Long userId);
}
