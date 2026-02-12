package ws.py3kl.playbook.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ws.py3kl.playbook.utils.SaltGenerator;
import ws.py3kl.playbook.utils.TokenGenerator;

@Configuration
public class CryptConfig {

    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SaltGenerator saltGenerator() {
        return () -> {
            byte[] saltBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(saltBytes);
            return java.util.Base64.getEncoder().encodeToString(saltBytes);
        };
    }

    @Bean
    public TokenGenerator tokenGenerator() {
        return () -> {
            byte[] tokenBytes = new byte[32];
            new java.security.SecureRandom().nextBytes(tokenBytes);
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        };
    }
}
