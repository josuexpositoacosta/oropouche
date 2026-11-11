package com.oropouche.tasks.infrastructure.controllers.paciente;

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

import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.in.PacienteUseCase;
import com.oropouche.tasks.infrastructure.controllers.paciente.dto.PacienteRequest;
import com.oropouche.tasks.infrastructure.controllers.paciente.dto.PacienteResponse;

/**
 * Adaptador de entrada REST para pacientes. Solo traduce HTTP ↔ caso de uso.
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@CrossOrigin(origins = {"http://localhost:4200"})
public class PacienteController {

    private final PacienteUseCase pacienteUseCase;

    public PacienteController(PacienteUseCase pacienteUseCase) {
        this.pacienteUseCase = pacienteUseCase;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> registrar(@RequestBody PacienteRequest request) {
        Paciente creado = pacienteUseCase.registrar(toDomain(request));
        return new ResponseEntity<>(toResponse(creado), HttpStatus.CREATED);
    }

    /**
     * Lista todos los pacientes, o solo los de un área si se indica {@code areaSaludId}.
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar(
            @RequestParam(required = false) Long areaSaludId) {

        List<Paciente> pacientes = areaSaludId == null
                ? pacienteUseCase.listarTodos()
                : pacienteUseCase.buscarPorAreaSalud(areaSaludId);

        return ResponseEntity.ok(pacientes.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(pacienteUseCase.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> actualizar(
            @PathVariable Long id, @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(toResponse(pacienteUseCase.actualizar(id, toDomain(request))));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<PacienteResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(pacienteUseCase.desactivar(id)));
    }

    private Paciente toDomain(PacienteRequest request) {
        Paciente paciente = new Paciente();
        paciente.setTipoDocumento(request.getTipoDocumento());
        paciente.setNumeroDocumento(request.getNumeroDocumento());
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setSexo(request.getSexo());
        paciente.setDireccion(request.getDireccion());
        paciente.setTelefono(request.getTelefono());
        paciente.setAreaSaludId(request.getAreaSaludId());
        return paciente;
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getTipoDocumento(),
                paciente.getNumeroDocumento(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getFechaNacimiento(),
                paciente.getSexo(),
                paciente.getDireccion(),
                paciente.getTelefono(),
                paciente.getAreaSaludId(),
                paciente.getFechaRegistro(),
                paciente.isActivo()
        );
    }
}
