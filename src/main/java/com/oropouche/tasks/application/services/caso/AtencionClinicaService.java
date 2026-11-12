package com.oropouche.tasks.application.services.caso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.ControlPendiente;
import com.oropouche.tasks.domain.models.caso.DefinicionCaso;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.EventoCaso;
import com.oropouche.tasks.domain.models.caso.EvolucionCaso;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;
import com.oropouche.tasks.domain.ports.in.AtencionClinicaUseCase;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.DesenlaceRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

import static com.oropouche.tasks.application.services.caso.CasoSoporte.esBlanco;

/**
 * Atención clínica del caso. Los registros son inmutables: la fecha y el usuario los pone el sistema,
 * y un error se corrige con un registro nuevo. Java puro.
 */
public class AtencionClinicaService implements AtencionClinicaUseCase {

    static final int MAX_TEXTO = 2000;

    private final CasoRepositoryPort casoRepositoryPort;
    private final DesenlaceRepositoryPort desenlaceRepositoryPort;
    private final UsuarioActualPort usuarioActualPort;
    private final CasoSoporte soporte;

    public AtencionClinicaService(
            CasoRepositoryPort casoRepositoryPort,
            DesenlaceRepositoryPort desenlaceRepositoryPort,
            UsuarioActualPort usuarioActualPort,
            DefinicionCaso definicionCaso,
            Clock clock) {

        this.casoRepositoryPort = casoRepositoryPort;
        this.desenlaceRepositoryPort = desenlaceRepositoryPort;
        this.usuarioActualPort = usuarioActualPort;
        this.soporte = new CasoSoporte(casoRepositoryPort, definicionCaso, clock);
    }

    @Override
    public EvaluacionCaso registrarEvaluacion(Long casoId, EvaluacionClinica evaluacion) {

        Caso caso = soporte.obtenerAbierto(casoId);

        if (evaluacion == null) {
            throw new IllegalArgumentException("Los datos de la evaluación son obligatorios");
        }
        if (!evaluacion.tieneSignosVitales() && evaluacion.getSignosClinicos().isEmpty()
                && evaluacion.getSignosAlarmaDengue().isEmpty() && esBlanco(evaluacion.getObservaciones())) {
            throw new IllegalArgumentException(
                    "La evaluación debe incluir al menos un signo vital, un signo clínico u observaciones");
        }
        validarSignosVitales(evaluacion);
        validarLongitud(evaluacion.getObservaciones(), "Las observaciones");

        evaluacion.setId(null);
        evaluacion.setFecha(soporte.ahora());
        evaluacion.setUsuario(usuarioActualPort.username());
        caso.getEvaluaciones().add(evaluacion);

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public EvaluacionCaso registrarDictamen(Long casoId, Dictamen dictamen) {

        Caso caso = soporte.obtenerAbierto(casoId);

        if (dictamen == null) {
            throw new IllegalArgumentException("Los datos del dictamen son obligatorios");
        }
        requerido(dictamen.getDiagnosticoPrincipal(), "El diagnóstico principal es obligatorio");
        requerido(dictamen.getConclusion(), "La conclusión es obligatoria");
        validarLongitud(dictamen.getDiagnosticoPrincipal(), "El diagnóstico principal");
        validarLongitud(dictamen.getDiagnosticosDiferenciales(), "Los diagnósticos diferenciales");
        validarLongitud(dictamen.getConclusion(), "La conclusión");

        caso.getDictamenes().add(new Dictamen(null, soporte.ahora(), dictamen.getDiagnosticoPrincipal().trim(),
                dictamen.getDiagnosticosDiferenciales(), dictamen.getConclusion().trim(), usuarioActualPort.username()));

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public EvaluacionCaso registrarPlan(Long casoId, PlanAtencion plan) {

        Caso caso = soporte.obtenerAbierto(casoId);

        if (plan == null || plan.getModalidad() == null) {
            throw new IllegalArgumentException("La modalidad de atención es obligatoria");
        }
        requerido(plan.getIndicaciones(), "Las indicaciones son obligatorias");
        validarLongitud(plan.getIndicaciones(), "Las indicaciones");
        validarProximoControl(plan.getProximoControl());

        caso.getPlanes().add(new PlanAtencion(null, soporte.ahora(), plan.getModalidad(),
                plan.getIndicaciones().trim(), plan.getProximoControl(), usuarioActualPort.username()));

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public EvaluacionCaso registrarSeguimiento(Long casoId, Seguimiento seguimiento) {

        Caso caso = soporte.obtenerAbierto(casoId);

        if (seguimiento == null || seguimiento.getEvolucion() == null) {
            throw new IllegalArgumentException("La evolución del caso es obligatoria");
        }
        validarLongitud(seguimiento.getObservaciones(), "Las observaciones");
        validarProximoControl(seguimiento.getProximoControl());

        caso.getSeguimientos().add(new Seguimiento(null, soporte.ahora(), seguimiento.getEvolucion(),
                seguimiento.getObservaciones(), seguimiento.getProximoControl(), usuarioActualPort.username()));

        if (seguimiento.getEvolucion() == EvolucionCaso.RECAIDA) {
            caso.setRecaida(true);
        }

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public EvaluacionCaso cerrar(Long casoId, String codigoDesenlace, LocalDate fechaCierre, String observaciones) {

        Caso caso = soporte.obtenerAbierto(casoId);

        if (esBlanco(codigoDesenlace)) {
            throw new IllegalArgumentException("El desenlace es obligatorio para cerrar el caso");
        }
        Desenlace desenlace = desenlaceRepositoryPort.findByCodigo(codigoDesenlace.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe el desenlace " + codigoDesenlace));
        if (!desenlace.isActivo()) {
            throw new IllegalArgumentException("El desenlace " + desenlace.getCodigo() + " está desactivado");
        }

        LocalDate fecha = fechaCierre == null ? soporte.hoy() : fechaCierre;
        if (fecha.isAfter(soporte.hoy())) {
            throw new IllegalArgumentException("La fecha de cierre no puede ser futura");
        }
        if (fecha.isBefore(caso.getFechaNotificacion())) {
            throw new IllegalArgumentException("La fecha de cierre no puede ser anterior a la notificación del caso");
        }
        validarLongitud(observaciones, "Las observaciones");

        caso.setEstado(EstadoCaso.CERRADO);
        caso.setDesenlace(desenlace);
        caso.setFechaCierre(fecha);
        caso.setObservacionesCierre(observaciones);
        caso.getEventos().add(new EventoCaso(null, soporte.ahora(), TipoEventoCaso.CIERRE,
                "Desenlace: " + desenlace.getNombre() + " (" + fecha + ")"
                        + (esBlanco(observaciones) ? "" : ". " + observaciones.trim()),
                usuarioActualPort.username()));

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public EvaluacionCaso reabrir(Long casoId, String motivo) {

        Caso caso = soporte.obtener(casoId);

        if (caso.estaAbierto()) {
            throw new IllegalArgumentException("El caso ya está abierto");
        }
        requerido(motivo, "El motivo de la reapertura es obligatorio");
        validarLongitud(motivo, "El motivo");
        if (casoRepositoryPort.existsAbiertoByPacienteId(caso.getPacienteId())) {
            throw new IllegalArgumentException("El paciente ya tiene otro caso abierto");
        }

        String cierreAnterior = caso.getDesenlace() == null ? "" :
                " (cierre anterior: " + caso.getDesenlace().getNombre() + ", " + caso.getFechaCierre() + ")";

        caso.setEstado(EstadoCaso.ABIERTO);
        caso.setDesenlace(null);
        caso.setFechaCierre(null);
        caso.setObservacionesCierre(null);
        caso.getEventos().add(new EventoCaso(null, soporte.ahora(), TipoEventoCaso.REAPERTURA,
                motivo.trim() + cierreAnterior, usuarioActualPort.username()));

        return soporte.guardarYEvaluar(caso);
    }

    @Override
    public List<ControlPendiente> controlesPendientes(Long areaSaludId, LocalDate hasta) {

        LocalDate limite = hasta == null ? soporte.hoy() : hasta;
        LocalDate hoy = soporte.hoy();

        List<ControlPendiente> pendientes = new ArrayList<>();
        for (Caso caso : casoRepositoryPort.findAll(areaSaludId, null, EstadoCaso.ABIERTO)) {
            LocalDate proximo = caso.proximoControl();
            if (proximo != null && !proximo.isAfter(limite)) {
                pendientes.add(new ControlPendiente(caso, proximo, proximo.isBefore(hoy)));
            }
        }
        pendientes.sort(Comparator.comparing(ControlPendiente::fecha));
        return pendientes;
    }

    private void validarProximoControl(LocalDate proximoControl) {
        if (proximoControl != null && proximoControl.isBefore(soporte.hoy())) {
            throw new IllegalArgumentException("La fecha del próximo control no puede ser pasada");
        }
    }

    private static void validarSignosVitales(EvaluacionClinica e) {
        if (e.getTemperatura() != null && (e.getTemperatura().compareTo(new BigDecimal("30")) < 0
                || e.getTemperatura().compareTo(new BigDecimal("45")) > 0)) {
            throw new IllegalArgumentException("La temperatura debe estar entre 30 y 45 °C");
        }
        rango(e.getFrecuenciaCardiaca(), 20, 250, "La frecuencia cardíaca");
        rango(e.getFrecuenciaRespiratoria(), 5, 80, "La frecuencia respiratoria");
        rango(e.getTensionSistolica(), 40, 300, "La tensión sistólica");
        rango(e.getTensionDiastolica(), 20, 200, "La tensión diastólica");
        rango(e.getSaturacionO2(), 50, 100, "La saturación de oxígeno");
        if (e.getTensionSistolica() != null && e.getTensionDiastolica() != null
                && e.getTensionSistolica() <= e.getTensionDiastolica()) {
            throw new IllegalArgumentException("La tensión sistólica debe ser mayor que la diastólica");
        }
    }

    private static void rango(Integer valor, int min, int max, String campo) {
        if (valor != null && (valor < min || valor > max)) {
            throw new IllegalArgumentException(campo + " debe estar entre " + min + " y " + max);
        }
    }

    private static void requerido(String valor, String mensaje) {
        if (esBlanco(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private static void validarLongitud(String valor, String campo) {
        if (valor != null && valor.length() > MAX_TEXTO) {
            throw new IllegalArgumentException(campo + " admite como máximo " + MAX_TEXTO + " caracteres");
        }
    }
}
