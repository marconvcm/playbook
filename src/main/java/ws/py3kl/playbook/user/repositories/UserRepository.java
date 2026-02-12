package ws.py3kl.playbook.user.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ws.py3kl.playbook.user.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByHandle(String handle);

    Optional<User> findByAccessToken(String accessToken);
}
