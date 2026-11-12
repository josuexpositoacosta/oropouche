package com.oropouche.tasks.infrastructure.controllers.admin.dto;

import com.oropouche.tasks.domain.models.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearUsuarioRequest {

    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String country;
    private Role role;
    private Long areaSaludId;
}
