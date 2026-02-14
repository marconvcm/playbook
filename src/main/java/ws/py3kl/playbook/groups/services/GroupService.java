package ws.py3kl.playbook.groups.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.py3kl.playbook.groups.exceptions.GroupNotFoundException;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.models.GroupMember;
import ws.py3kl.playbook.groups.models.responses.UserGroupsResponse;
import ws.py3kl.playbook.groups.models.requests.CreateGroupRequest;
import ws.py3kl.playbook.groups.models.requests.UpdateGroupRequest;
import ws.py3kl.playbook.groups.repositories.GroupMemberRepository;
import ws.py3kl.playbook.groups.repositories.GroupRepository;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static ws.py3kl.playbook.groups.exceptions.GroupFaultOperationException.groupIdAlreadyExists;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public List<Group> findAll() {
        return groupRepository.findAllByDeletedAtIsNull();
    }

    public Group findById(Long id) {
        return groupRepository.findById(id).orElseThrow(GroupNotFoundException::new);
    }

    public UserGroupsResponse findAllByUserId(Long userId) {
        return new UserGroupsResponse(
            groupRepository.findAllByOwnerIdAndDeletedAtIsNull(userId).stream().toList(),
            groupMemberRepository.findAllByUserId(userId).stream().map(GroupMember::getGroup).toList()
        );
    }

    @Transactional
    public Group create(User currentUser, CreateGroupRequest createGroupRequest) {

        if (groupRepository.existsByGroupId(createGroupRequest.getGroupId())) {
            throw groupIdAlreadyExists();
        }

        LocalDateTime now = LocalDateTime.now();

        Group group = new Group();
        group.setOwnerId(currentUser.getId());
        group.setGroupId(createGroupRequest.getGroupId());
        group.setTitle(createGroupRequest.getTitle());
        group.setSummary(createGroupRequest.getSummary());
        group.setCreatedAt(now);
        group.setUpdatedAt(now);

        return groupRepository.save(group);
    }

    @Transactional
    public Group update(Long id, User currentUser, UpdateGroupRequest updateGroupRequest) {
        Group group = findById(id)
            .checkAvailability()
            .checkOwnership(currentUser);

        group.setOwnerId(currentUser.getId());
        group.setTitle(updateGroupRequest.getTitle());
        group.setSummary(updateGroupRequest.getSummary());
        group.setUpdatedAt(LocalDateTime.now());

        return groupRepository.save(group);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Group group = findById(id)
            .checkAvailability()
            .checkOwnership(currentUser);

        group.setDeletedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);
    }

    @Transactional
    public GroupMember joinGroup(User currentUser, long groupId) {
        Group group = findById(groupId)
            .checkAvailability();

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())) {
            throw new IllegalStateException("User is already a member of this group");
        }

        LocalDateTime now = LocalDateTime.now();

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(currentUser);
        groupMember.setAdmitted(!group.getIsPrivate());
        groupMember.setCreatedAt(now);
        groupMember.setUpdatedAt(now);

        return groupMemberRepository.save(groupMember);
    }
}
