package com.oropouche.tasks.application.services.acceso;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.AccesoDenegadoException;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.ResultadoClasificacion;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.in.CasoUseCase;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CasoUseCaseConAccesoTest {

    @Mock
    private CasoUseCase delegado;

    @Mock
    private CasoRepositoryPort casos;

    @Mock
    private PacienteRepositoryPort pacientes;

    private CasoUseCaseConAcceso como(Role role, Long area) {
        return new CasoUseCaseConAcceso(delegado, casos, pacientes,
                new PoliticaAcceso(() -> Optional.of(new UsuarioActual(1, "u", role, area))));
    }

    private static Caso casoEnArea(Long area) {
        Caso c = new Caso();
        c.setId(10L);
        c.setAreaSaludId(area);
        return c;
    }

    private static EvaluacionCaso evaluacion(Caso c) {
        return new EvaluacionCaso(c, new ResultadoClasificacion(Optional.empty(), List.of()), List.of());
    }

    @Test
    void medicoNoRegistraCasoDePacienteDeOtraArea() {
        Paciente paciente = new Paciente();
        paciente.setAreaSaludId(4L);
        when(pacientes.findById(7L)).thenReturn(Optional.of(paciente));

        Caso datos = new Caso();
        datos.setPacienteId(7L);

        assertThrows(AccesoDenegadoException.class, () -> como(Role.MEDICO, 3L).registrar(datos, null));
        verify(delegado, never()).registrar(any(), any());
    }

    @Test
    void medicoNoVeCasoDeOtraAreaYElEpidemiologoSi() {
        when(delegado.buscarPorId(10L)).thenReturn(evaluacion(casoEnArea(4L)));

        assertThrows(AccesoDenegadoException.class, () -> como(Role.MEDICO, 3L).buscarPorId(10L));
        assertNotNull(como(Role.EPIDEMIOLOGO, null).buscarPorId(10L));
    }

    @Test
    void listarFuerzaElAreaDelMedico() {
        como(Role.MEDICO, 3L).listar(null, Clasificacion.CONFIRMADO, null);
        verify(delegado).listar(3L, Clasificacion.CONFIRMADO, null);
    }

    @Test
    void epidemiologoReclasificaEnCualquierAreaYElMedicoSoloEnLaSuya() {
        when(casos.findById(10L)).thenReturn(Optional.of(casoEnArea(4L)));

        assertThrows(AccesoDenegadoException.class,
                () -> como(Role.MEDICO, 3L).cambiarClasificacion(10L, Clasificacion.PROBABLE, "x"));

        como(Role.EPIDEMIOLOGO, null).cambiarClasificacion(10L, Clasificacion.PROBABLE, "x");
        verify(delegado).cambiarClasificacion(10L, Clasificacion.PROBABLE, "x");
    }

    @Test
    void soloElEpidemiologoValida() {
        assertThrows(AccesoDenegadoException.class, () -> como(Role.MEDICO, 3L).validarClasificacion(10L, null));
        como(Role.EPIDEMIOLOGO, null).validarClasificacion(10L, "ok");
        verify(delegado).validarClasificacion(10L, "ok");
    }
}
