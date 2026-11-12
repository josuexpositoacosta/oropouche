package com.oropouche.tasks.domain.models.user;

/** Usuario que realiza la operación en curso. */
public record UsuarioActual(Integer id, String username, Role role, Long areaSaludId) {

    public boolean es(Role otro) {
        return role == otro;
    }
}
