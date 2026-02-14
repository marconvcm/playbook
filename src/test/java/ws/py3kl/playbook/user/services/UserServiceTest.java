package ws.py3kl.playbook.user.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ws.py3kl.playbook.user.exceptions.WrongPasswordException;
import ws.py3kl.playbook.user.models.requests.CreateUserRequest;
import ws.py3kl.playbook.user.models.User;
import ws.py3kl.playbook.user.repositories.UserRepository;
import ws.py3kl.playbook.utils.SaltGenerator;
import ws.py3kl.playbook.utils.TokenGenerator;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SaltGenerator saltGenerator;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setHandle("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("hashedPassword");
        testUser.setPasswordSalt("salt123");
        testUser.setRoles(new String[]{"ROLE_USER"});
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setHandle("newuser");
        createUserRequest.setFirstName("New");
        createUserRequest.setLastName("User");
        createUserRequest.setPassword("password123");
    }

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
            userService.loadUserByUsername("notfound@example.com")
        );
        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void authenticate_ShouldReturnUserWithTokens_WhenCredentialsAreValid() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password + testUser.getPasswordSalt(), testUser.getPassword()))
            .thenReturn(true);
        when(tokenGenerator.generate())
            .thenReturn(accessToken)
            .thenReturn(refreshToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertNotNull(result.getLastLoginAt());
        assertNotNull(result.getTokenCreatedAt());
        assertNotNull(result.getTokenExpiresAt());

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password + testUser.getPasswordSalt(), testUser.getPassword());
        verify(tokenGenerator, times(2)).generate();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsWrong() {
        // Arrange
        String email = "test@example.com";
        String wrongPassword = "wrongpassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword + testUser.getPasswordSalt(), testUser.getPassword()))
            .thenReturn(false);

        // Act & Assert
        assertThrows(WrongPasswordException.class, () ->
            userService.authenticate(email, wrongPassword)
        );
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(wrongPassword + testUser.getPasswordSalt(), testUser.getPassword());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "notfound@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
            userService.authenticate(email, password)
        );
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void createUser_ShouldCreateAndReturnUser_WhenRequestIsValid() {
        // Arrange
        String salt = "generatedSalt";
        String hashedPassword = "hashedPassword123";

        when(saltGenerator.generate()).thenReturn(salt);
        when(passwordEncoder.encode(createUserRequest.getPassword() + salt)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // Act
        User result = userService.createUser(createUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createUserRequest.getEmail(), result.getEmail());
        assertEquals(createUserRequest.getHandle(), result.getHandle());
        assertEquals(createUserRequest.getFirstName(), result.getFirstName());
        assertEquals(createUserRequest.getLastName(), result.getLastName());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(salt, result.getPasswordSalt());
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().length);
        assertEquals("ROLE_USER", result.getRoles()[0]);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(saltGenerator).generate();
        verify(passwordEncoder).encode(createUserRequest.getPassword() + salt);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findByAccessToken_ShouldReturnUser_WhenTokenExists() {
        // Arrange
        String accessToken = "valid-token-123";
        testUser.setAccessToken(accessToken);

        when(userRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByAccessToken(accessToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(accessToken, result.get().getAccessToken());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByAccessToken(accessToken);
    }

    @Test
    void findByAccessToken_ShouldReturnEmpty_WhenTokenNotFound() {
        // Arrange
        String accessToken = "invalid-token";

        when(userRepository.findByAccessToken(accessToken)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByAccessToken(accessToken);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByAccessToken(accessToken);
    }

    @Test
    void authenticate_ShouldSetTokenExpirationTo30Minutes() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDateTime beforeAuth = LocalDateTime.now();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password + testUser.getPasswordSalt(), testUser.getPassword()))
            .thenReturn(true);
        when(tokenGenerator.generate()).thenReturn("token123").thenReturn("refresh123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertNotNull(result.getTokenExpiresAt());
        assertNotNull(result.getTokenCreatedAt());

        // Token should expire approximately 30 minutes from now
        LocalDateTime expectedExpiration = beforeAuth.plusMinutes(30);
        assertTrue(result.getTokenExpiresAt().isAfter(expectedExpiration.minusSeconds(5)));
        assertTrue(result.getTokenExpiresAt().isBefore(expectedExpiration.plusSeconds(5)));
    }
}