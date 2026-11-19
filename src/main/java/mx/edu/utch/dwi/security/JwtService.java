package mx.edu.utch.dwi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl,
            @Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String crearAccessToken(UserDetails user) {
        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un rol asignado"));
        return crearToken(user.getUsername(), role, "access", accessTokenTtl);
    }

    public String crearRefreshToken(UserDetails user) {
        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un rol asignado"));
        return crearToken(user.getUsername(), role, "refresh", refreshTokenTtl);
    }

    public TokenClaims validarAccessToken(String token) {
        return validarToken(token, "access");
    }

    public TokenClaims validarRefreshToken(String token) {
        return validarToken(token, "refresh");
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    private String crearToken(String subject, String role, String tokenUse, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .subject(subject)
                .claim("role", role)
                .claim("token_use", tokenUse)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    private TokenClaims validarToken(String token, String expectedUse) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenUse = claims.get("token_use", String.class);
        if (!expectedUse.equals(tokenUse)) {
            throw new JwtException("Tipo de token no permitido");
        }

        return new TokenClaims(
                claims.getSubject(),
                claims.get("role", String.class),
                claims.getId());
    }

    public record TokenClaims(String subject, String role, String tokenId) {
    }
}
