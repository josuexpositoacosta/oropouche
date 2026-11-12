package com.oropouche.tasks.infrastructure.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.AreaSaludUseCase;
import com.oropouche.tasks.domain.ports.in.GestionUsuariosUseCase;
import com.oropouche.tasks.infrastructure.jwt.JwtService;
import com.oropouche.tasks.infrastructure.security.UserDetailsAdapter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Reglas de acceso con la cadena de seguridad real (H2 en memoria): autenticación, roles y área.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    private static final AtomicInteger SECUENCIA = new AtomicInteger();
    private static final String PASSWORD = "Pw123456!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GestionUsuariosUseCase gestionUsuarios;

    @Autowired
    private AreaSaludUseCase areas;

    @Autowired
    private Environment environment;

    private Long areaA;
    private Long areaB;

    @BeforeEach
    void crearAreas() {
        areaA = crearArea();
        areaB = crearArea();
    }

    private Long crearArea() {
        int n = SECUENCIA.incrementAndGet();
        AreaSalud area = new AreaSalud();
        area.setCodigo("SEC-" + n);
        area.setNombre("Área de prueba " + n);
        area.setProvincia("P");
        area.setMunicipio("M");
        return areas.registrar(area).getId();
    }

    private User crearUsuario(Role role, Long areaSaludId) {
        return gestionUsuarios.crear(User.builder()
                .username(role.name().toLowerCase() + SECUENCIA.incrementAndGet())
                .password(PASSWORD)
                .firstname("Nombre")
                .lastname("Apellido")
                .role(role)
                .areaSaludId(areaSaludId)
                .build());
    }

    private String token(User usuario) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario.getUsername() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private static String paciente(String documento, Long areaSaludId) {
        return "{\"tipoDocumento\":\"CI\",\"numeroDocumento\":\"" + documento + "\",\"nombres\":\"Ana\","
                + "\"apellidos\":\"Pérez\",\"fechaNacimiento\":\"1990-01-01\",\"sexo\":\"F\","
                + "\"areaSaludId\":" + areaSaludId + "}";
    }

    // --- Autenticación

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/casos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/areas-salud")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/user/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoCaducadoOAjenoDevuelve401() throws Exception {
        User medico = crearUsuario(Role.MEDICO, areaA);
        UserDetailsAdapter detalles = new UserDetailsAdapter(medico);

        String ajeno = new JwtService("ZXN0YS1lcy1vdHJhLWNsYXZlLWRpc3RpbnRhLWRlLTMyLWJ5dGVz", 60_000).getToken(detalles);
        String caducado = new JwtService(environment.getRequiredProperty("jwt.secret"), -1_000).getToken(detalles);
        String fantasma = jwtService.getToken(new UserDetailsAdapter(
                User.builder().username("no_existe").role(Role.MEDICO).build()));

        for (String t : new String[] {"abc.def.ghi", ajeno, caducado, fantasma}) {
            mockMvc.perform(get("/api/v1/user/me").header("Authorization", "Bearer " + t))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void yaNoHayRegistroPublico() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"intruso\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioDesactivadoNoEntraNiConSuTokenAnterior() throws Exception {
        User medico = crearUsuario(Role.MEDICO, areaA);
        String token = token(medico);

        gestionUsuarios.actualizar(medico.getId(), Role.MEDICO, areaA, false);

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + medico.getUsername() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Roles

    @Test
    void adminGestionaUsuariosPeroNoVeDatosClinicos() throws Exception {
        String admin = token(crearUsuario(Role.ADMIN, null));
        int n = SECUENCIA.incrementAndGet();

        mockMvc.perform(get("/api/v1/admin/usuarios").header("Authorization", admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", admin)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/casos").header("Authorization", admin)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/areas-salud").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"ADM-" + n + "\",\"nombre\":\"Área admin " + n
                                + "\",\"provincia\":\"P\",\"municipio\":\"M\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void medicoNoAdministraNiGestionaCatalogos() throws Exception {
        String medico = token(crearUsuario(Role.MEDICO, areaA));

        mockMvc.perform(get("/api/v1/admin/usuarios").header("Authorization", medico)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/areas-salud").header("Authorization", medico)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/desenlaces").header("Authorization", medico)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"X1\",\"nombre\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void epidemiologoNoRegistraDatosClinicosYElMedicoNoValida() throws Exception {
        String epi = token(crearUsuario(Role.EPIDEMIOLOGO, null));
        String medico = token(crearUsuario(Role.MEDICO, areaA));

        mockMvc.perform(post("/api/v1/pacientes").header("Authorization", epi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paciente("EPI" + SECUENCIA.incrementAndGet(), areaA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/casos/1/clasificacion/validacion").header("Authorization", medico))
                .andExpect(status().isForbidden());
    }

    // --- Área de salud

    @Test
    void medicoSoloTrabajaEnSuAreaYElEpidemiologoVeTodas() throws Exception {
        String medicoA = token(crearUsuario(Role.MEDICO, areaA));
        String medicoB = token(crearUsuario(Role.MEDICO, areaB));
        String epi = token(crearUsuario(Role.EPIDEMIOLOGO, null));

        mockMvc.perform(post("/api/v1/pacientes").header("Authorization", medicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paciente("A" + SECUENCIA.incrementAndGet(), areaA)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/pacientes").header("Authorization", medicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paciente("B" + SECUENCIA.incrementAndGet(), areaB)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Solo puede acceder a datos de su área de salud"));
        mockMvc.perform(post("/api/v1/pacientes").header("Authorization", medicoB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paciente("B" + SECUENCIA.incrementAndGet(), areaB)))
                .andExpect(status().isCreated());

        // El médico A solo ve los pacientes de su área, aunque pida todos
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", medicoA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].areaSaludId", everyItem(equalTo(areaA.intValue()))));
        mockMvc.perform(get("/api/v1/pacientes").param("areaSaludId", areaB.toString()).header("Authorization", medicoA))
                .andExpect(status().isForbidden());

        // El epidemiólogo ve las dos áreas
        mockMvc.perform(get("/api/v1/pacientes").header("Authorization", epi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.areaSaludId == " + areaA + ")]").exists())
                .andExpect(jsonPath("$[?(@.areaSaludId == " + areaB + ")]").exists());
    }

    // --- Datos personales

    @Test
    void cadaUsuarioSoloVeSusDatosYElPerfilIncluyeRolYArea() throws Exception {
        User medico = crearUsuario(Role.MEDICO, areaA);
        User otro = crearUsuario(Role.MEDICO, areaA);
        String token = token(medico);

        mockMvc.perform(get("/api/v1/user/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(medico.getUsername()))
                .andExpect(jsonPath("$.role").value("MEDICO"))
                .andExpect(jsonPath("$.areaSaludId").value(areaA.intValue()));
        mockMvc.perform(get("/api/v1/user/" + otro.getId()).header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
