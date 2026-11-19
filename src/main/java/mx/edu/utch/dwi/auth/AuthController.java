package mx.edu.utch.dwi.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final UserAccountService userAccountService;

    public AuthController(AuthService authService, UserAccountService userAccountService) {
        this.authService = authService;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse registrar(@Valid @RequestBody RegisterRequest request) {
        userAccountService.registrar(request.email(), request.password());
        return new RegistrationResponse("Usuario registrado");
    }

    @PostMapping("/login")
    public TokenResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }

    @PostMapping("/refresh")
    public TokenResponse renovar(@Valid @RequestBody RefreshRequest request) {
        return authService.renovar(request.refreshToken());
    }

    public record RegistrationResponse(String message) {
    }
}
