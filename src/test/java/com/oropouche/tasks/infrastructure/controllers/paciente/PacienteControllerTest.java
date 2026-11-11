package com.oropouche.tasks.infrastructure.controllers.paciente;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.in.PacienteUseCase;
import com.oropouche.tasks.infrastructure.ExceptionHandler.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    @Mock
    private PacienteUseCase pacienteUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PacienteController(pacienteUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        // Igual que Spring Boot: fechas ISO ("1990-05-20"), no arrays
                        new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                .build()))
                .build();
    }

    private static Paciente paciente() {
        return new Paciente(1L, "CI", "123", "Juan", "Pérez",
                LocalDate.of(1990, 5, 20), "M", null, null,
                3L, LocalDateTime.of(2026, 11, 11, 10, 0), true);
    }

    @Test
    void registrarDebeDevolver201() throws Exception {

        when(pacienteUseCase.registrar(any(Paciente.class))).thenReturn(paciente());

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoDocumento\":\"CI\",\"numeroDocumento\":\"123\",\"nombres\":\"Juan\","
                                + "\"apellidos\":\"Pérez\",\"fechaNacimiento\":\"1990-05-20\",\"sexo\":\"M\","
                                + "\"areaSaludId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fechaNacimiento").value("1990-05-20"))
                .andExpect(jsonPath("$.areaSaludId").value(3));
    }

    @Test
    void registrarEnAreaInvalidaDebeDevolver400() throws Exception {

        when(pacienteUseCase.registrar(any(Paciente.class)))
                .thenThrow(new IllegalArgumentException("El área de salud AS-2 está desactivada"));

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"areaSaludId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El área de salud AS-2 está desactivada"));
    }

    @Test
    void buscarInexistenteDebeDevolver404() throws Exception {

        when(pacienteUseCase.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

        mockMvc.perform(get("/api/v1/pacientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarConAreaDebeFiltrar() throws Exception {

        when(pacienteUseCase.buscarPorAreaSalud(3L)).thenReturn(List.of(paciente()));

        mockMvc.perform(get("/api/v1/pacientes").param("areaSaludId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(pacienteUseCase, never()).listarTodos();
    }

    @Test
    void desactivarDebeDevolverPacienteInactivo() throws Exception {

        Paciente inactivo = paciente();
        inactivo.setActivo(false);
        when(pacienteUseCase.desactivar(1L)).thenReturn(inactivo);

        mockMvc.perform(patch("/api/v1/pacientes/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
