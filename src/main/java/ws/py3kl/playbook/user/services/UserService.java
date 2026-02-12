package ws.py3kl.playbook.user.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ws.py3kl.playbook.user.exceptions.WrongPasswordException;
import ws.py3kl.playbook.user.models.CreateUserRequest;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.repositories.UserRepository;
import ws.py3kl.playbook.utils.SaltGenerator;
import ws.py3kl.playbook.utils.TokenGenerator;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SaltGenerator saltGenerator;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Transactional
    public User authenticate(String email, String password) {
        LocalDateTime now = LocalDateTime.now();
        User user = (User) loadUserByUsername(email);
        if (passwordEncoder.matches(password + user.getPasswordSalt(), user.getPassword())) {
            user.setLastLoginAt(LocalDateTime.now());
            user.setAccessToken(tokenGenerator.generate());
            user.setRefreshToken(tokenGenerator.generate());
            user.setTokenCreatedAt(now);
            user.setTokenExpiresAt(now.plusMinutes(30));
            userRepository.save(user);
            return user;
        } else {
            throw new WrongPasswordException();
        }
    }

    @Transactional
    public User createUser(CreateUserRequest createUserRequest) {

        LocalDateTime now = LocalDateTime.now();

        String salt = saltGenerator.generate();
        String password = passwordEncoder.encode(createUserRequest.getPassword() + salt);

        User user = new User();
        user.setEmail(createUserRequest.getEmail());
        user.setHandle(createUserRequest.getHandle());
        user.setPassword(password);
        user.setPasswordSalt(salt);
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRoles(getDefaultRoles());

        return userRepository.save(user);
    }

    private String[] getDefaultRoles() {
        return new String[]{"ROLE_USER"};
    }

    public Optional<User> findByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken);
    }
}
