package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;

/**
 * Puerto de entrada: gestión de usuarios por el administrador.
 */
public interface GestionUsuariosUseCase {

    /** Crea un usuario. {@code nuevo.password} viene en texto plano. */
    User crear(User nuevo);

    List<User> listar();

    /** Cambia rol, área y estado. No permite dejar el sistema sin administradores activos. */
    User actualizar(Integer id, Role role, Long areaSaludId, boolean activo);

    void restablecerPassword(Integer id, String nuevaPassword);

    /** Crea el primer administrador si no existe ninguno activo. Devuelve true si lo creó. */
    boolean crearAdminInicialSiNoExiste(String username, String password);
}
