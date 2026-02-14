package ws.py3kl.playbook.groups.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.models.requests.CreateGroupRequest;
import ws.py3kl.playbook.groups.models.requests.UpdateGroupRequest;
import ws.py3kl.playbook.groups.repositories.GroupRepository;
import ws.py3kl.playbook.user.models.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public List<Group> findAll() {
        return groupRepository.findAll()
            .stream()
            .filter(group -> group.getDeletedAt() == null)
            .toList();
    }

    public List<Group> findOwnedByUser(Long ownerId) {
        return groupRepository.findByOwnerId(ownerId)
            .stream()
            .filter(group -> group.getDeletedAt() == null)
            .toList();
    }

    public Group findById(Long id) {
        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (group.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        return group;
    }

    @Transactional
    public Group create(User currentUser, CreateGroupRequest createGroupRequest) {
        groupRepository.findByGroupId(createGroupRequest.getGroupId())
            .ifPresent(group -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group ID already exists");
            });

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
        Group group = findById(id);

        if (!group.getOwnerId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this group");
        }

        group.setTitle(updateGroupRequest.getTitle());
        group.setSummary(updateGroupRequest.getSummary());
        group.setUpdatedAt(LocalDateTime.now());

        return groupRepository.save(group);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Group group = findById(id);

        if (!group.getOwnerId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this group");
        }

        group.setDeletedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);
    }
}
