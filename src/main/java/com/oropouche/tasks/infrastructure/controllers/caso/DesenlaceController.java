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

import com.oropouche.tasks.domain.ports.in.DesenlaceUseCase;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.DesenlaceRequest;
import com.oropouche.tasks.infrastructure.controllers.caso.dto.DesenlaceResponse;

/**
 * Catálogo editable de desenlaces de cierre de caso.
 */
@RestController
@RequestMapping("/api/v1/desenlaces")
@CrossOrigin(origins = {"http://localhost:4200"})
public class DesenlaceController {

    private final DesenlaceUseCase desenlaceUseCase;

    public DesenlaceController(DesenlaceUseCase desenlaceUseCase) {
        this.desenlaceUseCase = desenlaceUseCase;
    }

    @GetMapping
    public ResponseEntity<List<DesenlaceResponse>> listar(
            @RequestParam(defaultValue = "false") boolean soloActivos) {

        return ResponseEntity.ok(desenlaceUseCase.listar(soloActivos).stream()
                .map(DesenlaceResponse::from)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DesenlaceResponse> crear(@RequestBody DesenlaceRequest r) {
        return new ResponseEntity<>(
                DesenlaceResponse.from(desenlaceUseCase.crear(r.getCodigo(), r.getNombre())), HttpStatus.CREATED);
    }

    /** Solo se modifica el nombre: el código es estable porque lo usan los casos cerrados. */
    @PutMapping("/{id}")
    public ResponseEntity<DesenlaceResponse> renombrar(@PathVariable Long id, @RequestBody DesenlaceRequest r) {
        return ResponseEntity.ok(DesenlaceResponse.from(desenlaceUseCase.renombrar(id, r.getNombre())));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<DesenlaceResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(DesenlaceResponse.from(desenlaceUseCase.cambiarEstado(id, false)));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<DesenlaceResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(DesenlaceResponse.from(desenlaceUseCase.cambiarEstado(id, true)));
    }
}
