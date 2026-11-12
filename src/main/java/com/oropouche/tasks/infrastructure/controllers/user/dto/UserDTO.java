package com.oropouche.tasks.infrastructure.controllers.user.dto;

import com.oropouche.tasks.domain.models.user.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    int id;
    String username;
    String firstname;
    String lastname;
    String country;
    Role role;
    Long areaSaludId;
}
