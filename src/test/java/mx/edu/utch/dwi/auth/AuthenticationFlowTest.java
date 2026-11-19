package mx.edu.utch.dwi.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.access-token-ttl=PT15M",
        "app.jwt.refresh-token-ttl=P7D"
})
class AuthenticationFlowTest {
    private static final String PASSWORD = "Test-Pass-2026!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginValidoDevuelveAccessYRefreshToken() throws Exception {
        String email = registrarUsuario();

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void rutaDeAlumnosAceptaAccessTokenValido() throws Exception {
        String accessToken = iniciarSesion(registrarUsuario());

        mockMvc.perform(get("/api/alumnos")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void rutaDeAlumnosRechazaSolicitudSinToken() throws Exception {
        mockMvc.perform(get("/api/alumnos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rutaDeAlumnosRechazaTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/alumnos")
                        .header("Authorization", "Bearer token_invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void usuarioSinRolAdminNoPuedeEliminarAlumnos() throws Exception {
        String accessToken = iniciarSesion(registrarUsuario());

        mockMvc.perform(delete("/api/alumnos/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void refreshTokenSeRotaYElAnteriorNoSePuedeReutilizar() throws Exception {
        String email = registrarUsuario();
        JsonNode tokens = iniciarSesionConTokens(email);
        String refreshToken = tokens.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode rotatedTokens = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        mockMvc.perform(post("/api/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/alumnos")
                        .header("Authorization", "Bearer " + rotatedTokens.get("accessToken").asText()))
                .andExpect(status().isOk());
    }

    private String registrarUsuario() throws Exception {
        String email = "alumno-" + UUID.randomUUID() + "@utch.test";
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, PASSWORD))))
                .andExpect(status().isCreated());
        return email;
    }

    private String iniciarSesion(String email) throws Exception {
        return iniciarSesionConTokens(email).get("accessToken").asText();
    }

    private JsonNode iniciarSesionConTokens(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String loginBody(String email) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequest(email, PASSWORD));
    }
}
