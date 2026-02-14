package ws.py3kl.playbook.ui.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.py3kl.playbook.groups.models.Group;
import ws.py3kl.playbook.groups.services.GroupService;
import ws.py3kl.playbook.posts.models.Post;
import ws.py3kl.playbook.posts.services.PostService;
import ws.py3kl.playbook.user.models.User;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfilePageDataService {

    @Autowired
    private PostService postService;

    @Autowired
    private GroupService groupService;

    public List<Post> findUserPosts(User currentUser) {
        return postService.findByUserId(currentUser.getId());
    }

    public List<Group> findOwnedGroups(User currentUser) {
        return groupService.findOwnedByUser(currentUser.getId());
    }

    public List<Group> findMemberGroups(User currentUser) {
        List<Post> userPosts = findUserPosts(currentUser);
        List<Group> ownedGroups = findOwnedGroups(currentUser);

        Set<Long> ownedGroupIds = ownedGroups.stream().map(Group::getId).collect(Collectors.toSet());
        Set<Long> postedGroupIds = userPosts.stream()
            .map(Post::getGroupId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());

        return groupService.findAll().stream()
            .filter(group -> postedGroupIds.contains(group.getId()) && !ownedGroupIds.contains(group.getId()))
            .sorted(Comparator.comparing(Group::getId))
            .toList();
    }
}
