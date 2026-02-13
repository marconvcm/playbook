package ws.py3kl.playbook.groups.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.models.requests.CreateGroupRequest;
import ws.py3kl.playbook.groups.models.requests.UpdateGroupRequest;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.utils.CurrentUser;
import ws.py3kl.playbook.utils.AcknowledgeResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping
    public AcknowledgeResponse<List<Group>> getGroups() {
        return AcknowledgeResponse.of(groupService.findAll(), "Groups fetched successfully");
    }

    @GetMapping("/{id}")
    public AcknowledgeResponse<Group> getGroup(@PathVariable Long id) {
        return AcknowledgeResponse.of(groupService.findById(id), "Group fetched successfully");
    }

    @PostMapping
    public AcknowledgeResponse<Group> createGroup(@CurrentUser User currentUser, @Valid @RequestBody CreateGroupRequest createGroupRequest) {
        return AcknowledgeResponse.of(groupService.create(currentUser, createGroupRequest), "Group created successfully");
    }

    @PutMapping("/{id}")
    public AcknowledgeResponse<Group> updateGroup(@PathVariable Long id, @CurrentUser User currentUser, @Valid @RequestBody UpdateGroupRequest updateGroupRequest) {
        return AcknowledgeResponse.of(groupService.update(id, currentUser, updateGroupRequest), "Group updated successfully");
    }

    @DeleteMapping("/{id}")
    public AcknowledgeResponse<Boolean> deleteGroup(@PathVariable Long id, @CurrentUser User currentUser) {
        groupService.delete(id, currentUser);
        return AcknowledgeResponse.of(true, "Group deleted successfully");
    }
}
