package mx.edu.utch.dwi.alumnos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.utch.dwi.auth.LoginRequest;
import mx.edu.utch.dwi.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlumnoControllerTest {
    private static final String PASSWORD = "Test-Pass-2026!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarAlumnosConTokenDevuelveLista() throws Exception {
        mockMvc.perform(get("/api/alumnos")
                        .header("Authorization", "Bearer " + obtenerAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void crearAlumnoConTokenDevuelve201YRepresentacionCreada() throws Exception {
        mockMvc.perform(post("/api/alumnos")
                        .header("Authorization", "Bearer " + obtenerAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Derek Siqueiros",
                                  "matricula": "UTCH-0001",
                                  "carrera": "Tecnologias de la Informacion"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre", is("Derek Siqueiros")))
                .andExpect(jsonPath("$.matricula", is("UTCH-0001")));
    }

    private String obtenerAccessToken() throws Exception {
        String email = "alumno-" + UUID.randomUUID() + "@utch.test";
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, PASSWORD))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }
}
