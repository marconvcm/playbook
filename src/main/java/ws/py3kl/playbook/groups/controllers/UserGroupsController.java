package ws.py3kl.playbook.groups.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.utils.CurrentUser;
import ws.py3kl.playbook.utils.AcknowledgeResponse;

import java.util.List;

@RestController
public class UserGroupsController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/v1/users/me/groups")
    public AcknowledgeResponse<List<Group>> getPosts(@CurrentUser User user) {
        return AcknowledgeResponse.of(groupService.findAllByUserId(user.getId()), "Groups fetched successfully");
    }

    @GetMapping("/v1/users/{id}/groups")
    public AcknowledgeResponse<List<Group>> getPosts(@PathVariable("id") Long id) {
        return AcknowledgeResponse.of(groupService.findAllByUserId(id), "Groups fetched successfully");
    }
}
