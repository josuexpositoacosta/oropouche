package com.oropouche.tasks.infrastructure.controllers.admin.dto;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;

/** Datos de un usuario para administración. Nunca incluye la contraseña. */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Integer id;
    private String username;
    private String firstname;
    private String lastname;
    private String country;
    private Role role;
    private Long areaSaludId;
    private boolean activo;

    public static UsuarioResponse from(User u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getFirstname(), u.getLastname(),
                u.getCountry(), u.getRole(), u.getAreaSaludId(), u.isActivo());
    }
}
