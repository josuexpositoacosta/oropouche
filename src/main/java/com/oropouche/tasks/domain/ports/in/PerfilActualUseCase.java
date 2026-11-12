package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.user.User;

/** Puerto de entrada: datos del usuario autenticado (para que el frontend conozca su rol y área). */
public interface PerfilActualUseCase {

    User perfilActual();
}
