package ws.py3kl.playbook.groups.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.models.GroupMember;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findAllByUserId(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
