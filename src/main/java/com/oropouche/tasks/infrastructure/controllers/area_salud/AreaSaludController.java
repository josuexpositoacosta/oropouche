package com.oropouche.tasks.infrastructure.controllers.area_salud;

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
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.ports.in.AreaSaludUseCase;
import com.oropouche.tasks.infrastructure.controllers.area_salud.dto.AreaSaludRequest;
import com.oropouche.tasks.infrastructure.controllers.area_salud.dto.AreaSaludResponse;

/**
 * Adaptador de entrada REST para áreas de salud. Solo traduce HTTP ↔ caso de uso.
 */
@RestController
@RequestMapping("/api/v1/areas-salud")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AreaSaludController {

    private final AreaSaludUseCase areaSaludUseCase;

    public AreaSaludController(AreaSaludUseCase areaSaludUseCase) {
        this.areaSaludUseCase = areaSaludUseCase;
    }

    @PostMapping
    public ResponseEntity<AreaSaludResponse> registrar(@RequestBody AreaSaludRequest request) {
        AreaSalud creada = areaSaludUseCase.registrar(toDomain(request));
        return new ResponseEntity<>(toResponse(creada), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AreaSaludResponse>> listarTodas() {
        return ResponseEntity.ok(toResponses(areaSaludUseCase.listarTodas()));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<AreaSaludResponse>> listarActivas() {
        return ResponseEntity.ok(toResponses(areaSaludUseCase.listarActivas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaSaludResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(areaSaludUseCase.buscarPorId(id)));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AreaSaludResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(toResponse(areaSaludUseCase.buscarPorCodigo(codigo)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaSaludResponse> actualizar(
            @PathVariable Long id, @RequestBody AreaSaludRequest request) {
        return ResponseEntity.ok(toResponse(areaSaludUseCase.actualizar(id, toDomain(request))));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<AreaSaludResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(areaSaludUseCase.desactivar(id)));
    }

    private AreaSalud toDomain(AreaSaludRequest request) {
        AreaSalud area = new AreaSalud();
        area.setCodigo(request.getCodigo());
        area.setNombre(request.getNombre());
        area.setProvincia(request.getProvincia());
        area.setMunicipio(request.getMunicipio());
        area.setDireccion(request.getDireccion());
        area.setTelefono(request.getTelefono());
        return area;
    }

    private AreaSaludResponse toResponse(AreaSalud area) {
        return new AreaSaludResponse(
                area.getId(),
                area.getCodigo(),
                area.getNombre(),
                area.getProvincia(),
                area.getMunicipio(),
                area.getDireccion(),
                area.getTelefono(),
                area.isActivo(),
                area.getFechaRegistro()
        );
    }

    private List<AreaSaludResponse> toResponses(List<AreaSalud> areas) {
        return areas.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
