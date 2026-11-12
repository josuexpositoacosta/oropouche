package com.oropouche.tasks.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.infrastructure.jwt.JwtService;
import com.oropouche.tasks.infrastructure.security.UserDetailsAdapter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica las reglas de acceso con la cadena de seguridad real (H2 en memoria).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String registrarYObtenerToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Pw123456!\","
                                + "\"firstname\":\"A\",\"lastname\":\"B\",\"country\":\"CU\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void getSinTokenDebeDevolver401() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/areas-salud")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/user/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/casos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/desenlaces")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/areas-salud/activas")).andExpect(status().isUnauthorized());
    }

    @Test
    void postSinTokenDebeDevolver401() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalFormadoDebeDevolver401YNo500() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtraClaveDebeDevolver401() throws Exception {
        String ajeno = new JwtService("ZXN0YS1lcy1vdHJhLWNsYXZlLWRpc3RpbnRhLWRlLTMyLWJ5dGVz", 60_000)
                .getToken(new UserDetailsAdapter(User.builder().username("x").role(Role.USER).build()));

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", "Bearer " + ajeno))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenCaducadoDebeDevolver401() throws Exception {
        registrarYObtenerToken("caducado_user");
        String caducado = new JwtService(secretoDeTest(), -1_000)
                .getToken(new UserDetailsAdapter(
                        User.builder().username("caducado_user").role(Role.USER).build()));

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", "Bearer " + caducado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioInexistenteDebeDevolver401() throws Exception {
        String fantasma = jwtService.getToken(new UserDetailsAdapter(
                User.builder().username("no_existe_en_bd").role(Role.USER).build()));

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", "Bearer " + fantasma))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenValidoDebePermitirAcceso() throws Exception {
        String token = registrarYObtenerToken("seguridad_ok");
        assertTrue(token.split("\\.").length == 3, "debe ser un JWT");

        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginDebeSerPublico() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nadie\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuario o contraseña incorrectos"));
    }

    /** Misma clave que src/test/resources/application.properties, leída del propio contexto. */
    @Autowired
    private org.springframework.core.env.Environment environment;

    private String secretoDeTest() {
        return environment.getRequiredProperty("jwt.secret");
    }
}
