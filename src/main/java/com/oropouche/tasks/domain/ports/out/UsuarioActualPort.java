package com.oropouche.tasks.domain.ports.out;

/**
 * Puerto de salida: identidad del usuario que realiza la operación (para auditoría).
 */
public interface UsuarioActualPort {

    /** Nombre del usuario autenticado, o "sistema" si no hay ninguno. */
    String username();
}
