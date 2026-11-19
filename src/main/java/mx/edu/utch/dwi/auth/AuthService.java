package mx.edu.utch.dwi.auth;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import mx.edu.utch.dwi.security.JwtService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;
    private final ConcurrentMap<String, String> refreshSessions = new ConcurrentHashMap<>();

    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountService userAccountService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
    }

    public TokenResponse iniciarSesion(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim(), request.password()));
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        } catch (AuthenticationServiceException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No fue posible autenticar al usuario");
        }

        return emitirTokens((UserDetails) authentication.getPrincipal());
    }

    public TokenResponse renovar(String refreshToken) {
        JwtService.TokenClaims claims;
        try {
            claims = jwtService.validarRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }

        String registeredUser = refreshSessions.remove(claims.tokenId());
        if (registeredUser == null || !registeredUser.equals(claims.subject())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o ya utilizado");
        }

        UserDetails user = userAccountService.loadUserByUsername(claims.subject());
        return emitirTokens(user);
    }

    private TokenResponse emitirTokens(UserDetails user) {
        String accessToken = jwtService.crearAccessToken(user);
        String refreshToken = jwtService.crearRefreshToken(user);
        String refreshTokenId = jwtService.validarRefreshToken(refreshToken).tokenId();
        refreshSessions.put(refreshTokenId, user.getUsername());

        return new TokenResponse("Bearer", accessToken, refreshToken, jwtService.accessTokenTtlSeconds());
    }
}
