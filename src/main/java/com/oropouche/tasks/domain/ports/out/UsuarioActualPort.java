package com.oropouche.tasks.domain.ports.out;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.UsuarioActual;

/**
 * Puerto de salida: identidad del usuario que realiza la operación.
 */
public interface UsuarioActualPort {

    /** Usuario autenticado; vacío si no hay ninguno (por ejemplo, tareas de arranque). */
    Optional<UsuarioActual> actual();

    /** Nombre del usuario autenticado, o "sistema" si no hay ninguno. Para auditoría. */
    default String username() {
        return actual().map(UsuarioActual::username).orElse("sistema");
    }
}
