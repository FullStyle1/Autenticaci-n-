package mx.edu.utch.dwi.auth;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserAccountService implements UserDetailsService {
    private final ConcurrentMap<String, UserDetails> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.admin-email:}") String adminEmail,
            @Value("${app.auth.admin-password:}") String adminPassword) {
        this.passwordEncoder = passwordEncoder;
        if (adminEmail.isBlank() != adminPassword.isBlank()) {
            throw new IllegalStateException("Configura APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD juntos");
        }
        if (!adminEmail.isBlank()) {
            String normalizedEmail = normalize(adminEmail);
            UserDetails admin = User.withUsername(normalizedEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles("ADMIN")
                    .build();
            users.put(normalizedEmail, admin);
        }
    }

    public void registrar(String email, String rawPassword) {
        String normalizedEmail = normalize(email);
        UserDetails user = User.withUsername(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .roles("USER")
                .build();

        if (users.putIfAbsent(normalizedEmail, user) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(normalize(username));
        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return user;
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
