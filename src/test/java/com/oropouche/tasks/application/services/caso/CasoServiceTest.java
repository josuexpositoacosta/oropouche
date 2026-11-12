package com.oropouche.tasks.application.services.caso;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.ClasificadorOPS;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CasoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 12);

    @Mock
    private CasoRepositoryPort casoRepository;

    @Mock
    private PacienteRepositoryPort pacienteRepository;

    @Mock
    private UsuarioActualPort usuarioActual;

    private CasoService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(HOY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new CasoService(casoRepository, pacienteRepository, usuarioActual, new ClasificadorOPS(), clock);
    }

    private static Paciente paciente(boolean activo) {
        Paciente p = new Paciente();
        p.setId(7L);
        p.setAreaSaludId(3L);
        p.setActivo(activo);
        return p;
    }

    /** Datos de un caso que cumple la definición de sospechoso. */
    private static Caso datosSospechoso() {
        Caso c = new Caso();
        c.setPacienteId(7L);
        c.setFechaInicioSintomas(HOY.minusDays(2));
        c.setFechaNotificacion(HOY);
        c.setFiebre(true);
        c.setCefaleaIntensa(true);
        c.setManifestaciones(EnumSet.of(Manifestacion.MIALGIA_ARTRALGIA, Manifestacion.ESCALOFRIOS));
        return c;
    }

    private static Caso casoGuardado(Clasificacion clasificacion, EstadoCaso estado) {
        Caso c = datosSospechoso();
        c.setId(1L);
        c.setAreaSaludId(3L);
        c.setClasificacion(clasificacion);
        c.setEstado(estado);
        return c;
    }

    private static ResultadoLaboratorio rtPcr(ResultadoPrueba resultado) {
        return new ResultadoLaboratorio(null, AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR,
                TipoMuestra.SUERO, resultado, HOY.minusDays(1), HOY);
    }

    private void guardarDevuelveLoMismo() {
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- Registro

    @Test
    void registrarAsignaClasificacionSugeridaAreaDelPacienteEHistorialInicial() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));
        when(usuarioActual.username()).thenReturn("dra.perez");
        guardarDevuelveLoMismo();

        Caso datos = datosSospechoso();
        datos.setAreaTransmisionOViaje(true);

        EvaluacionCaso r = service.registrar(datos, null);

        assertEquals(Clasificacion.PROBABLE, r.caso().getClasificacion());
        assertEquals(EstadoCaso.ABIERTO, r.caso().getEstado());
        assertEquals(3L, r.caso().getAreaSaludId(), "el caso se abre en el área del paciente");
        assertEquals(1, r.caso().getHistorial().size());
        assertNull(r.caso().getHistorial().get(0).getAnterior());
        assertEquals("dra.perez", r.caso().getHistorial().get(0).getUsuario());
    }

    @Test
    void registrarSinFechaNotificacionUsaHoy() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));
        guardarDevuelveLoMismo();

        Caso datos = datosSospechoso();
        datos.setFechaNotificacion(null);

        assertEquals(HOY, service.registrar(datos, null).caso().getFechaNotificacion());
    }

    @Test
    void registrarSinCumplirDefinicionExigeMotivo() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));

        Caso datos = datosSospechoso();
        datos.setCefaleaIntensa(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(datos, " "));
        assertTrue(ex.getMessage().contains("indique un motivo"));
        verify(casoRepository, never()).save(any());
    }

    @Test
    void registrarSinCumplirDefinicionConMotivoQuedaSospechoso() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));
        guardarDevuelveLoMismo();

        Caso datos = datosSospechoso();
        datos.setCefaleaIntensa(false);

        EvaluacionCaso r = service.registrar(datos, "Cuadro atípico en zona con brote activo");

        assertEquals(Clasificacion.SOSPECHOSO, r.caso().getClasificacion());
        assertTrue(r.sugerencia().sugerida().isEmpty());
        assertEquals("Cuadro atípico en zona con brote activo", r.caso().getHistorial().get(0).getMotivo());
    }

    @Test
    void registrarRechazaPacienteInexistenteODesactivado() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.registrar(datosSospechoso(), null));

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(false)));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(datosSospechoso(), null));
        assertTrue(ex.getMessage().contains("desactivado"));
    }

    @Test
    void registrarRechazaSegundoCasoAbierto() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));
        when(casoRepository.existsAbiertoByPacienteId(7L)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> service.registrar(datosSospechoso(), null));
        verify(casoRepository, never()).save(any());
    }

    @Test
    void registrarValidaFechas() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));

        Caso futuro = datosSospechoso();
        futuro.setFechaNotificacion(HOY.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(futuro, null));

        Caso invertido = datosSospechoso();
        invertido.setFechaInicioSintomas(HOY);
        invertido.setFechaNotificacion(HOY.minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(invertido, null));

        Caso sinInicio = datosSospechoso();
        sinInicio.setFechaInicioSintomas(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinInicio, null));
    }

    @Test
    void registrarValidaSemanasDeGestacion() {

        when(pacienteRepository.findById(7L)).thenReturn(Optional.of(paciente(true)));

        Caso noEmbarazada = datosSospechoso();
        noEmbarazada.setSemanasGestacion(20);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(noEmbarazada, null));

        Caso fueraDeRango = datosSospechoso();
        fueraDeRango.setEmbarazada(true);
        fueraDeRango.setSemanasGestacion(50);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(fueraDeRango, null));
    }

    // --- Resultados de laboratorio

    @Test
    void registrarResultadoRecalculaSugerenciaSinCambiarClasificacion() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.registrarResultado(1L, rtPcr(ResultadoPrueba.POSITIVO));

        assertEquals(Clasificacion.SOSPECHOSO, r.caso().getClasificacion(), "lo decide el profesional");
        assertEquals(Optional.of(Clasificacion.CONFIRMADO), r.sugerencia().sugerida());
        assertEquals(1, r.caso().getResultados().size());
    }

    @Test
    void registrarResultadoValidaDatosYFechas() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));

        ResultadoLaboratorio sinTipo = rtPcr(ResultadoPrueba.POSITIVO);
        sinTipo.setTipoPrueba(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrarResultado(1L, sinTipo));

        ResultadoLaboratorio resultadoAntesDeToma = rtPcr(ResultadoPrueba.POSITIVO);
        resultadoAntesDeToma.setFechaResultado(HOY.minusDays(5));
        assertThrows(IllegalArgumentException.class, () -> service.registrarResultado(1L, resultadoAntesDeToma));

        ResultadoLaboratorio futuro = rtPcr(ResultadoPrueba.POSITIVO);
        futuro.setFechaToma(HOY.plusDays(1));
        futuro.setFechaResultado(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrarResultado(1L, futuro));

        verify(casoRepository, never()).save(any());
    }

    @Test
    void casoCerradoNoAdmiteCambios() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.CERRADO)));

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarResultado(1L, rtPcr(ResultadoPrueba.POSITIVO)));
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarClasificacion(1L, Clasificacion.DESCARTADO, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, datosSospechoso()));
    }

    // --- Cambio de clasificación

    @Test
    void confirmarSinLaboratorioConfirmatorioSeRechaza() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarClasificacion(1L, Clasificacion.CONFIRMADO, "Clínica compatible"));
        assertTrue(ex.getMessage().contains("laboratorio confirmatorio"));
        verify(casoRepository, never()).save(any());
    }

    @Test
    void confirmarConRtPcrPositivaRegistraHistorial() {

        Caso caso = casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO);
        caso.getResultados().add(rtPcr(ResultadoPrueba.POSITIVO));
        when(casoRepository.findById(1L)).thenReturn(Optional.of(caso));
        when(usuarioActual.username()).thenReturn("epi.gomez");
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.cambiarClasificacion(1L, Clasificacion.CONFIRMADO, " RT-PCR positiva ");

        assertEquals(Clasificacion.CONFIRMADO, r.caso().getClasificacion());

        ArgumentCaptor<Caso> guardado = ArgumentCaptor.forClass(Caso.class);
        verify(casoRepository).save(guardado.capture());
        var ultimo = guardado.getValue().getHistorial().get(guardado.getValue().getHistorial().size() - 1);
        assertEquals(Clasificacion.SOSPECHOSO, ultimo.getAnterior());
        assertEquals(Clasificacion.CONFIRMADO, ultimo.getNueva());
        assertEquals("RT-PCR positiva", ultimo.getMotivo());
        assertEquals("epi.gomez", ultimo.getUsuario());
    }

    @Test
    void descartarExigeMotivo() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarClasificacion(1L, Clasificacion.DESCARTADO, null));
        verify(casoRepository, never()).save(any());
    }

    @Test
    void descartarConMotivoSePermiteAunqueDifieraDeLaSugerencia() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.cambiarClasificacion(1L, Clasificacion.DESCARTADO,
                "RT-PCR Oropouche negativa y dengue confirmado");

        assertEquals(Clasificacion.DESCARTADO, r.caso().getClasificacion());
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), r.sugerencia().sugerida());
    }

    @Test
    void cambiarALaMismaClasificacionSeRechaza() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarClasificacion(1L, Clasificacion.SOSPECHOSO, "x"));
    }

    // --- Consultas

    @Test
    void casoInexistenteDevuelveNoEncontrado() {

        when(casoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void listarPorPacienteInexistenteDevuelveNoEncontrado() {

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.listarPorPaciente(99L));
    }

    @Test
    void actualizarConservaClasificacionYPacientePeroRecalculaSugerencia() {

        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO)));
        guardarDevuelveLoMismo();

        Caso cambios = datosSospechoso();
        cambios.setPacienteId(999L);
        cambios.setNexoCasoConfirmado(true);

        EvaluacionCaso r = service.actualizar(1L, cambios);

        assertEquals(7L, r.caso().getPacienteId(), "no se puede cambiar el paciente");
        assertEquals(Clasificacion.SOSPECHOSO, r.caso().getClasificacion());
        assertEquals(Optional.of(Clasificacion.PROBABLE), r.sugerencia().sugerida());
        assertTrue(r.caso().isNexoCasoConfirmado());
    }

    // --- Validación por epidemiología

    @Test
    void cambioDeUnMedicoQuedaPendienteYElDelEpidemiologoValidado() {

        Caso caso = casoGuardado(Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO);
        caso.setClasificacionValidada(true);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(caso));
        guardarDevuelveLoMismo();

        when(usuarioActual.actual()).thenReturn(Optional.of(new UsuarioActual(1, "dr", Role.MEDICO, 3L)));
        assertFalse(service.cambiarClasificacion(1L, Clasificacion.DESCARTADO, "Dengue confirmado")
                .caso().isClasificacionValidada(), "lo que propone el médico queda pendiente de validar");

        when(usuarioActual.actual()).thenReturn(Optional.of(new UsuarioActual(2, "epi", Role.EPIDEMIOLOGO, null)));
        assertTrue(service.cambiarClasificacion(1L, Clasificacion.SOSPECHOSO, "Revisión epidemiológica")
                .caso().isClasificacionValidada());
    }

    @Test
    void validarRegistraEventoYNoSeRepite() {

        Caso caso = casoGuardado(Clasificacion.PROBABLE, EstadoCaso.CERRADO);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(caso));
        when(usuarioActual.username()).thenReturn("epi.gomez");
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.validarClasificacion(1L, "Nexo confirmado");

        assertTrue(r.caso().isClasificacionValidada(), "se puede validar aunque el caso esté cerrado");
        assertEquals(TipoEventoCaso.VALIDACION_CLASIFICACION, r.caso().getEventos().get(0).getTipo());
        assertTrue(r.caso().getEventos().get(0).getDetalle().contains("PROBABLE validada. Nexo confirmado"));
        assertEquals("epi.gomez", r.caso().getEventos().get(0).getUsuario());

        assertThrows(IllegalArgumentException.class, () -> service.validarClasificacion(1L, null));
    }
}
