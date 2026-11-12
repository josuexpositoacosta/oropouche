package com.oropouche.tasks.application.services.caso;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.caso.CambioClasificacion;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.DefinicionCaso;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.ResultadoClasificacion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.in.CasoUseCase;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

/**
 * Casos de uso del módulo de casos. Java puro: no depende de ningún framework.
 */
public class CasoService implements CasoUseCase {

    static final int MAX_SEMANAS_GESTACION = 45;

    private final CasoRepositoryPort casoRepositoryPort;
    private final PacienteRepositoryPort pacienteRepositoryPort;
    private final UsuarioActualPort usuarioActualPort;
    private final DefinicionCaso definicionCaso;
    private final Clock clock;

    public CasoService(
            CasoRepositoryPort casoRepositoryPort,
            PacienteRepositoryPort pacienteRepositoryPort,
            UsuarioActualPort usuarioActualPort,
            DefinicionCaso definicionCaso,
            Clock clock) {

        this.casoRepositoryPort = casoRepositoryPort;
        this.pacienteRepositoryPort = pacienteRepositoryPort;
        this.usuarioActualPort = usuarioActualPort;
        this.definicionCaso = definicionCaso;
        this.clock = clock;
    }

    @Override
    public EvaluacionCaso registrar(Caso datos, String motivo) {

        if (datos == null) {
            throw new IllegalArgumentException("Los datos del caso son obligatorios");
        }
        if (datos.getPacienteId() == null) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }

        Paciente paciente = pacienteRepositoryPort.findById(datos.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el paciente con ID: " + datos.getPacienteId()));

        if (!paciente.isActivo()) {
            throw new IllegalArgumentException("El paciente está desactivado");
        }
        if (casoRepositoryPort.existsAbiertoByPacienteId(paciente.getId())) {
            throw new RecursoDuplicadoException(
                    "El paciente ya tiene un caso abierto; registre la recaída en ese caso");
        }

        if (datos.getFechaNotificacion() == null) {
            datos.setFechaNotificacion(hoy());
        }
        validarDatosClinicos(datos);

        Caso caso = new Caso();
        copiarDatosClinicos(datos, caso);
        caso.setPacienteId(paciente.getId());
        caso.setAreaSaludId(paciente.getAreaSaludId());
        caso.setEstado(EstadoCaso.ABIERTO);

        ResultadoClasificacion sugerencia = definicionCaso.evaluar(caso);

        Clasificacion inicial;
        String motivoInicial;
        if (sugerencia.sugerida().isPresent()) {
            inicial = sugerencia.sugerida().get();
            motivoInicial = esBlanco(motivo)
                    ? "Registro inicial: clasificación sugerida por " + definicionCaso.nombre()
                    : motivo.trim();
        } else {
            if (esBlanco(motivo)) {
                throw new IllegalArgumentException(
                        "El caso no cumple la definición de caso sospechoso de " + definicionCaso.nombre()
                                + "; indique un motivo para registrarlo igualmente");
            }
            inicial = Clasificacion.SOSPECHOSO;
            motivoInicial = motivo.trim();
        }

        caso.setClasificacion(inicial);
        caso.getHistorial().add(cambio(null, inicial, motivoInicial));

        return evaluar(casoRepositoryPort.save(caso));
    }

    @Override
    public EvaluacionCaso buscarPorId(Long id) {
        return evaluar(obtener(id));
    }

    @Override
    public List<EvaluacionCaso> listar(Long areaSaludId, Clasificacion clasificacion, EstadoCaso estado) {
        return casoRepositoryPort.findAll(areaSaludId, clasificacion, estado).stream()
                .map(this::evaluar)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluacionCaso> listarPorPaciente(Long pacienteId) {

        if (pacienteId == null || pacienteRepositoryPort.findById(pacienteId).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe el paciente con ID: " + pacienteId);
        }

        return casoRepositoryPort.findByPacienteId(pacienteId).stream()
                .map(this::evaluar)
                .collect(Collectors.toList());
    }

    @Override
    public EvaluacionCaso actualizar(Long id, Caso datos) {

        Caso caso = obtenerAbierto(id);

        if (datos == null) {
            throw new IllegalArgumentException("Los datos del caso son obligatorios");
        }
        if (datos.getFechaNotificacion() == null) {
            datos.setFechaNotificacion(caso.getFechaNotificacion());
        }
        validarDatosClinicos(datos);
        copiarDatosClinicos(datos, caso);

        return evaluar(casoRepositoryPort.save(caso));
    }

    @Override
    public EvaluacionCaso registrarResultado(Long casoId, ResultadoLaboratorio resultado) {

        Caso caso = obtenerAbierto(casoId);
        validarResultado(resultado);

        resultado.setId(null);
        caso.getResultados().add(resultado);

        return evaluar(casoRepositoryPort.save(caso));
    }

    @Override
    public EvaluacionCaso cambiarClasificacion(Long casoId, Clasificacion nueva, String motivo) {

        Caso caso = obtenerAbierto(casoId);

        if (nueva == null) {
            throw new IllegalArgumentException("La nueva clasificación es obligatoria");
        }
        if (esBlanco(motivo)) {
            throw new IllegalArgumentException("El motivo del cambio de clasificación es obligatorio");
        }
        if (nueva == caso.getClasificacion()) {
            throw new IllegalArgumentException("El caso ya tiene la clasificación " + nueva);
        }

        ResultadoClasificacion sugerencia = definicionCaso.evaluar(caso);
        if (nueva == Clasificacion.CONFIRMADO
                && sugerencia.sugerida().filter(c -> c == Clasificacion.CONFIRMADO).isEmpty()) {
            throw new IllegalArgumentException(
                    "Para confirmar el caso debe cumplir la definición de caso sospechoso y tener"
                            + " un resultado de laboratorio confirmatorio de Oropouche positivo");
        }

        caso.getHistorial().add(cambio(caso.getClasificacion(), nueva, motivo.trim()));
        caso.setClasificacion(nueva);

        return evaluar(casoRepositoryPort.save(caso));
    }

    private Caso obtener(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador del caso no es válido");
        }

        return casoRepositoryPort.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el caso con ID: " + id));
    }

    private Caso obtenerAbierto(Long id) {

        Caso caso = obtener(id);

        if (!caso.estaAbierto()) {
            throw new IllegalArgumentException("El caso está cerrado y no admite cambios");
        }

        return caso;
    }

    private EvaluacionCaso evaluar(Caso caso) {
        return new EvaluacionCaso(caso, definicionCaso.evaluar(caso));
    }

    private CambioClasificacion cambio(Clasificacion anterior, Clasificacion nueva, String motivo) {
        return new CambioClasificacion(
                null, LocalDateTime.now(clock), anterior, nueva, motivo, usuarioActualPort.username());
    }

    private void validarDatosClinicos(Caso datos) {

        LocalDate hoy = hoy();

        if (datos.getFechaInicioSintomas() == null) {
            throw new IllegalArgumentException("La fecha de inicio de síntomas es obligatoria");
        }
        if (datos.getFechaInicioSintomas().isAfter(hoy) || datos.getFechaNotificacion().isAfter(hoy)) {
            throw new IllegalArgumentException("Las fechas del caso no pueden ser futuras");
        }
        if (datos.getFechaInicioSintomas().isAfter(datos.getFechaNotificacion())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio de síntomas no puede ser posterior a la de notificación");
        }

        Integer semanas = datos.getSemanasGestacion();
        if (!datos.isEmbarazada() && semanas != null) {
            throw new IllegalArgumentException("Solo se indican semanas de gestación si la paciente está embarazada");
        }
        if (semanas != null && (semanas < 1 || semanas > MAX_SEMANAS_GESTACION)) {
            throw new IllegalArgumentException(
                    "Las semanas de gestación deben estar entre 1 y " + MAX_SEMANAS_GESTACION);
        }
    }

    private void validarResultado(ResultadoLaboratorio resultado) {

        if (resultado == null) {
            throw new IllegalArgumentException("Los datos del resultado son obligatorios");
        }
        if (resultado.getAgente() == null || resultado.getTipoPrueba() == null
                || resultado.getTipoMuestra() == null || resultado.getResultado() == null) {
            throw new IllegalArgumentException(
                    "Agente, tipo de prueba, tipo de muestra y resultado son obligatorios");
        }
        if (resultado.getFechaToma() == null) {
            throw new IllegalArgumentException("La fecha de toma de la muestra es obligatoria");
        }

        LocalDate hoy = hoy();
        if (resultado.getFechaToma().isAfter(hoy)
                || (resultado.getFechaResultado() != null && resultado.getFechaResultado().isAfter(hoy))) {
            throw new IllegalArgumentException("Las fechas del resultado no pueden ser futuras");
        }
        if (resultado.getFechaResultado() != null
                && resultado.getFechaResultado().isBefore(resultado.getFechaToma())) {
            throw new IllegalArgumentException("La fecha del resultado no puede ser anterior a la toma de muestra");
        }
    }

    private static void copiarDatosClinicos(Caso origen, Caso destino) {
        destino.setFechaInicioSintomas(origen.getFechaInicioSintomas());
        destino.setFechaNotificacion(origen.getFechaNotificacion());
        destino.setFiebre(origen.isFiebre());
        destino.setCefaleaIntensa(origen.isCefaleaIntensa());
        destino.setManifestaciones(origen.getManifestaciones());
        destino.setRecaida(origen.isRecaida());
        destino.setEmbarazada(origen.isEmbarazada());
        destino.setSemanasGestacion(origen.getSemanasGestacion());
        destino.setAreaTransmisionOViaje(origen.isAreaTransmisionOViaje());
        destino.setDetalleAreaTransmision(origen.getDetalleAreaTransmision());
        destino.setNexoCasoConfirmado(origen.isNexoCasoConfirmado());
    }

    private LocalDate hoy() {
        return LocalDate.now(clock);
    }

    private static boolean esBlanco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
