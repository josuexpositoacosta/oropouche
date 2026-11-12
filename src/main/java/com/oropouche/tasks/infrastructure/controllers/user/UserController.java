package com.oropouche.tasks.infrastructure.controllers.user;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.GetUserUseCase;
import com.oropouche.tasks.domain.ports.in.PerfilActualUseCase;
import com.oropouche.tasks.domain.ports.in.UpdateUserUseCase;
import com.oropouche.tasks.infrastructure.controllers.user.dto.UserDTO;
import com.oropouche.tasks.infrastructure.controllers.user.dto.UserRequest;
import com.oropouche.tasks.infrastructure.controllers.user.dto.UserResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para usuarios. Solo traduce HTTP ↔ casos de uso.
 */
@RestController
@RequestMapping(value = "/api/v1/user")
@CrossOrigin(origins = {"http://localhost:4200"})
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final PerfilActualUseCase perfilActualUseCase;

    public UserController(GetUserUseCase getUserUseCase, UpdateUserUseCase updateUserUseCase,
                          PerfilActualUseCase perfilActualUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.perfilActualUseCase = perfilActualUseCase;
    }

    /** Perfil del usuario autenticado: id, nombre, rol y área. Lo usa el frontend tras el login. */
    @GetMapping(value = "me")
    public ResponseEntity<UserDTO> perfilActual() {
        return ResponseEntity.ok(toDTO(perfilActualUseCase.perfilActual()));
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer id) {

        return getUserUseCase.getUser(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping()
    public ResponseEntity<UserResponse> updateUser(
            @RequestBody UserRequest userRequest) {

        return updateUserUseCase.updateUser(toDomain(userRequest))
                .map(user -> ResponseEntity.ok(
                        new UserResponse("El usuario se actualizó satisfactoriamente")))
                .orElse(ResponseEntity.notFound().build());
    }

    private UserDTO toDTO(User user) {

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .country(user.getCountry())
                .role(user.getRole())
                .areaSaludId(user.getAreaSaludId())
                .build();
    }

    private User toDomain(UserRequest request) {

        return User.builder()
                .id(request.getId())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .country(request.getCountry())
                .build();
    }
}
