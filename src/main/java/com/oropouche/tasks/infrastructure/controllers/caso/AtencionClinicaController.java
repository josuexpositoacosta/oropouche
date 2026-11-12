package com.oropouche.tasks.infrastructure.controllers.caso;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;
import com.oropouche.tasks.domain.ports.in.AtencionClinicaUseCase;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.CasoResponse;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.CierreRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.ControlPendienteResponse;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.DictamenRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.EvaluacionClinicaRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.MotivoRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.PlanAtencionRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.SeguimientoRequest;

/**
 * Adaptador de entrada REST para la atención clínica, el cierre y la reapertura de casos.
 */
@RestController
@RequestMapping("/api/v1/casos")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AtencionClinicaController {

    private final AtencionClinicaUseCase atencionClinicaUseCase;

    public AtencionClinicaController(AtencionClinicaUseCase atencionClinicaUseCase) {
        this.atencionClinicaUseCase = atencionClinicaUseCase;
    }

    @PostMapping("/{id}/evaluaciones")
    public ResponseEntity<CasoResponse> registrarEvaluacion(
            @PathVariable Long id, @RequestBody EvaluacionClinicaRequest r) {

        EvaluacionClinica evaluacion = new EvaluacionClinica();
        evaluacion.setTemperatura(r.getTemperatura());
        evaluacion.setFrecuenciaCardiaca(r.getFrecuenciaCardiaca());
        evaluacion.setFrecuenciaRespiratoria(r.getFrecuenciaRespiratoria());
        evaluacion.setTensionSistolica(r.getTensionSistolica());
        evaluacion.setTensionDiastolica(r.getTensionDiastolica());
        evaluacion.setSaturacionO2(r.getSaturacionO2());
        evaluacion.setSignosClinicos(r.getSignosClinicos());
        evaluacion.setSignosAlarmaDengue(r.getSignosAlarmaDengue());
        evaluacion.setObservaciones(r.getObservaciones());

        return creado(atencionClinicaUseCase.registrarEvaluacion(id, evaluacion));
    }

    @PostMapping("/{id}/dictamenes")
    public ResponseEntity<CasoResponse> registrarDictamen(@PathVariable Long id, @RequestBody DictamenRequest r) {
        return creado(atencionClinicaUseCase.registrarDictamen(id,
                new Dictamen(null, null, r.getDiagnosticoPrincipal(), r.getDiagnosticosDiferenciales(),
                        r.getConclusion(), null)));
    }

    @PostMapping("/{id}/planes")
    public ResponseEntity<CasoResponse> registrarPlan(@PathVariable Long id, @RequestBody PlanAtencionRequest r) {
        return creado(atencionClinicaUseCase.registrarPlan(id,
                new PlanAtencion(null, null, r.getModalidad(), r.getIndicaciones(), r.getProximoControl(), null)));
    }

    @PostMapping("/{id}/seguimientos")
    public ResponseEntity<CasoResponse> registrarSeguimiento(@PathVariable Long id, @RequestBody SeguimientoRequest r) {
        return creado(atencionClinicaUseCase.registrarSeguimiento(id,
                new Seguimiento(null, null, r.getEvolucion(), r.getObservaciones(), r.getProximoControl(), null)));
    }

    @PatchMapping("/{id}/cierre")
    public ResponseEntity<CasoResponse> cerrar(@PathVariable Long id, @RequestBody CierreRequest r) {
        return ResponseEntity.ok(CasoResponse.from(
                atencionClinicaUseCase.cerrar(id, r.getDesenlace(), r.getFechaCierre(), r.getObservaciones())));
    }

    @PatchMapping("/{id}/reapertura")
    public ResponseEntity<CasoResponse> reabrir(@PathVariable Long id, @RequestBody MotivoRequest r) {
        return ResponseEntity.ok(CasoResponse.from(atencionClinicaUseCase.reabrir(id, r.getMotivo())));
    }

    @GetMapping("/controles-pendientes")
    public ResponseEntity<List<ControlPendienteResponse>> controlesPendientes(
            @RequestParam(required = false) Long areaSaludId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(atencionClinicaUseCase.controlesPendientes(areaSaludId, hasta).stream()
                .map(ControlPendienteResponse::from)
                .collect(Collectors.toList()));
    }

    private static ResponseEntity<CasoResponse> creado(EvaluacionCaso evaluacion) {
        return new ResponseEntity<>(CasoResponse.from(evaluacion), HttpStatus.CREATED);
    }
}
