package com.oropouche.tasks.infrastructure.controllers.admin;

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

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.GestionUsuariosUseCase;
import com.oropouche.tasks.infrastructure.controllers.admin.dto.ActualizarUsuarioRequest;
import com.oropouche.tasks.infrastructure.controllers.admin.dto.CrearUsuarioRequest;
import com.oropouche.tasks.infrastructure.controllers.admin.dto.PasswordRequest;
import com.oropouche.tasks.infrastructure.controllers.admin.dto.UsuarioResponse;

/**
 * Administración de usuarios (solo ADMIN; la regla está en SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AdminUsuariosController {

    private final GestionUsuariosUseCase gestionUsuarios;

    public AdminUsuariosController(GestionUsuariosUseCase gestionUsuarios) {
        this.gestionUsuarios = gestionUsuarios;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(gestionUsuarios.listar().stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestBody CrearUsuarioRequest r) {
        User creado = gestionUsuarios.crear(User.builder()
                .username(r.getUsername())
                .password(r.getPassword())
                .firstname(r.getFirstname())
                .lastname(r.getLastname())
                .country(r.getCountry())
                .role(r.getRole())
                .areaSaludId(r.getAreaSaludId())
                .build());
        return new ResponseEntity<>(UsuarioResponse.from(creado), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Integer id, @RequestBody ActualizarUsuarioRequest r) {
        return ResponseEntity.ok(UsuarioResponse.from(
                gestionUsuarios.actualizar(id, r.getRole(), r.getAreaSaludId(), r.isActivo())));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> restablecerPassword(@PathVariable Integer id, @RequestBody PasswordRequest r) {
        gestionUsuarios.restablecerPassword(id, r.getPassword());
        return ResponseEntity.noContent().build();
    }
}
