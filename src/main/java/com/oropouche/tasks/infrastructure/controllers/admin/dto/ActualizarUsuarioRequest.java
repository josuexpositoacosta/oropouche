package com.oropouche.tasks.infrastructure.controllers.admin.dto;

import com.oropouche.tasks.domain.models.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioRequest {

    private Role role;
    private Long areaSaludId;
    private boolean activo;
}
