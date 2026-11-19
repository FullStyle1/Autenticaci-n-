package mx.edu.utch.dwi.auth;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn) {
}
