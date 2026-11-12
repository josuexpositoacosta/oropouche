package com.oropouche.tasks.infrastructure.controllers.caso;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ResultadoClasificacion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.ports.in.CasoUseCase;
import com.oropouche.tasks.infrastructure.ExceptionHandler.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CasoControllerTest {

    @Mock
    private CasoUseCase casoUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CasoController(casoUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                .build()))
                .build();
    }

    private static EvaluacionCaso evaluacion(Clasificacion actual, Clasificacion sugerida) {
        Caso c = new Caso();
        c.setId(1L);
        c.setPacienteId(7L);
        c.setAreaSaludId(3L);
        c.setFechaInicioSintomas(LocalDate.of(2026, 11, 10));
        c.setFechaNotificacion(LocalDate.of(2026, 11, 12));
        c.setFiebre(true);
        c.setCefaleaIntensa(true);
        c.setManifestaciones(EnumSet.of(Manifestacion.MAREOS, Manifestacion.ESCALOFRIOS));
        c.setClasificacion(actual);
        c.setEstado(EstadoCaso.ABIERTO);
        return new EvaluacionCaso(c, new ResultadoClasificacion(Optional.ofNullable(sugerida), List.of("Cefalea intensa")));
    }

    @Test
    void registrarDevuelve201ConSugerencia() throws Exception {

        when(casoUseCase.registrar(any(Caso.class), any()))
                .thenReturn(evaluacion(Clasificacion.SOSPECHOSO, Clasificacion.SOSPECHOSO));

        mockMvc.perform(post("/api/v1/casos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":7,\"fechaInicioSintomas\":\"2026-11-10\",\"fiebre\":true,"
                                + "\"cefaleaIntensa\":true,\"manifestaciones\":[\"MAREOS\",\"ESCALOFRIOS\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clasificacion").value("SOSPECHOSO"))
                .andExpect(jsonPath("$.sugerencia.clasificacion").value("SOSPECHOSO"))
                .andExpect(jsonPath("$.sugerencia.coincideConActual").value(true))
                .andExpect(jsonPath("$.sugerencia.criterios[0]").value("Cefalea intensa"))
                .andExpect(jsonPath("$.fechaNotificacion").value("2026-11-12"));

        ArgumentCaptor<Caso> enviado = ArgumentCaptor.forClass(Caso.class);
        verify(casoUseCase).registrar(enviado.capture(), any());
        assertEquals(EnumSet.of(Manifestacion.MAREOS, Manifestacion.ESCALOFRIOS), enviado.getValue().getManifestaciones());
    }

    @Test
    void manifestacionInexistenteDevuelve400() throws Exception {

        mockMvc.perform(post("/api/v1/casos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":7,\"manifestaciones\":[\"TOS\"]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(casoUseCase);
    }

    @Test
    void filtroDeClasificacionInvalidoDevuelve400() throws Exception {

        mockMvc.perform(get("/api/v1/casos").param("clasificacion", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Valor no válido para el parámetro: clasificacion"));
    }

    @Test
    void listarPasaLosFiltros() throws Exception {

        when(casoUseCase.listar(3L, Clasificacion.CONFIRMADO, EstadoCaso.ABIERTO))
                .thenReturn(List.of(evaluacion(Clasificacion.CONFIRMADO, Clasificacion.CONFIRMADO)));

        mockMvc.perform(get("/api/v1/casos")
                        .param("areaSaludId", "3").param("clasificacion", "CONFIRMADO").param("estado", "ABIERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void registrarResultadoDevuelve201() throws Exception {

        when(casoUseCase.registrarResultado(eq(1L), any(ResultadoLaboratorio.class)))
                .thenReturn(evaluacion(Clasificacion.SOSPECHOSO, Clasificacion.CONFIRMADO));

        mockMvc.perform(post("/api/v1/casos/1/resultados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agente\":\"OROPOUCHE\",\"tipoPrueba\":\"RT_PCR\",\"tipoMuestra\":\"SUERO\","
                                + "\"resultado\":\"POSITIVO\",\"fechaToma\":\"2026-11-11\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sugerencia.clasificacion").value("CONFIRMADO"))
                .andExpect(jsonPath("$.sugerencia.coincideConActual").value(false));
    }

    @Test
    void confirmarSinLaboratorioDevuelve400ConMensaje() throws Exception {

        when(casoUseCase.cambiarClasificacion(1L, Clasificacion.CONFIRMADO, "Clínica"))
                .thenThrow(new IllegalArgumentException("Para confirmar el caso debe tener laboratorio"));

        mockMvc.perform(patch("/api/v1/casos/1/clasificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clasificacion\":\"CONFIRMADO\",\"motivo\":\"Clínica\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Para confirmar el caso debe tener laboratorio"));
    }

    @Test
    void casosDePacienteUsaLaRutaAnidada() throws Exception {

        when(casoUseCase.listarPorPaciente(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pacientes/7/casos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
