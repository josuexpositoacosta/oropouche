package com.oropouche.tasks.infrastructure.controllers.caso;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.ports.in.CasoUseCase;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.CambioClasificacionRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.CasoRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.CasoResponse;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.ResultadoLaboratorioRequest;

/**
 * Adaptador de entrada REST para casos de Oropouche. Solo traduce HTTP ↔ caso de uso.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:4200"})
public class CasoController {

    private final CasoUseCase casoUseCase;

    public CasoController(CasoUseCase casoUseCase) {
        this.casoUseCase = casoUseCase;
    }

    @PostMapping("/casos")
    public ResponseEntity<CasoResponse> registrar(@RequestBody CasoRequest request) {
        EvaluacionCaso creado = casoUseCase.registrar(toDomain(request), request.getMotivo());
        return new ResponseEntity<>(CasoResponse.from(creado), HttpStatus.CREATED);
    }

    @GetMapping("/casos")
    public ResponseEntity<List<CasoResponse>> listar(
            @RequestParam(required = false) Long areaSaludId,
            @RequestParam(required = false) Clasificacion clasificacion,
            @RequestParam(required = false) EstadoCaso estado) {

        return ResponseEntity.ok(toResponses(casoUseCase.listar(areaSaludId, clasificacion, estado)));
    }

    @GetMapping("/casos/{id}")
    public ResponseEntity<CasoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(CasoResponse.from(casoUseCase.buscarPorId(id)));
    }

    @GetMapping("/pacientes/{pacienteId}/casos")
    public ResponseEntity<List<CasoResponse>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(toResponses(casoUseCase.listarPorPaciente(pacienteId)));
    }

    @PutMapping("/casos/{id}")
    public ResponseEntity<CasoResponse> actualizar(@PathVariable Long id, @RequestBody CasoRequest request) {
        return ResponseEntity.ok(CasoResponse.from(casoUseCase.actualizar(id, toDomain(request))));
    }

    @PostMapping("/casos/{id}/resultados")
    public ResponseEntity<CasoResponse> registrarResultado(
            @PathVariable Long id, @RequestBody ResultadoLaboratorioRequest request) {

        ResultadoLaboratorio resultado = new ResultadoLaboratorio(null, request.getAgente(),
                request.getTipoPrueba(), request.getTipoMuestra(), request.getResultado(),
                request.getFechaToma(), request.getFechaResultado());

        return new ResponseEntity<>(
                CasoResponse.from(casoUseCase.registrarResultado(id, resultado)), HttpStatus.CREATED);
    }

    @PatchMapping("/casos/{id}/clasificacion")
    public ResponseEntity<CasoResponse> cambiarClasificacion(
            @PathVariable Long id, @RequestBody CambioClasificacionRequest request) {

        return ResponseEntity.ok(CasoResponse.from(
                casoUseCase.cambiarClasificacion(id, request.getClasificacion(), request.getMotivo())));
    }

    private Caso toDomain(CasoRequest r) {
        Caso caso = new Caso();
        caso.setPacienteId(r.getPacienteId());
        caso.setFechaInicioSintomas(r.getFechaInicioSintomas());
        caso.setFechaNotificacion(r.getFechaNotificacion());
        caso.setFiebre(r.isFiebre());
        caso.setCefaleaIntensa(r.isCefaleaIntensa());
        caso.setManifestaciones(r.getManifestaciones());
        caso.setRecaida(r.isRecaida());
        caso.setEmbarazada(r.isEmbarazada());
        caso.setSemanasGestacion(r.getSemanasGestacion());
        caso.setAreaTransmisionOViaje(r.isAreaTransmisionOViaje());
        caso.setDetalleAreaTransmision(r.getDetalleAreaTransmision());
        caso.setNexoCasoConfirmado(r.isNexoCasoConfirmado());
        return caso;
    }

    private static List<CasoResponse> toResponses(List<EvaluacionCaso> evaluaciones) {
        return evaluaciones.stream().map(CasoResponse::from).collect(Collectors.toList());
    }
}
