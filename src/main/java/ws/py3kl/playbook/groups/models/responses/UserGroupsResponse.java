package ws.py3kl.playbook.groups.models.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ws.py3kl.playbook.groups.models.Group;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupsResponse {

    private List<Group> ownerOf;
    private List<Group> memberOf;
}
