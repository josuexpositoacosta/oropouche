package com.oropouche.tasks.application.services.acceso;

import java.util.Objects;
import java.util.Optional;

import com.oropouche.tasks.domain.exceptions.AccesoDenegadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.in.GetUserUseCase;
import com.oropouche.tasks.domain.ports.in.PerfilActualUseCase;
import com.oropouche.tasks.domain.ports.in.UpdateUserUseCase;

/** Cada usuario solo consulta y modifica sus propios datos personales; el ADMIN, los de cualquiera. */
public class UsuarioPropioConAcceso implements GetUserUseCase, UpdateUserUseCase, PerfilActualUseCase {

    private final GetUserUseCase getUser;
    private final UpdateUserUseCase updateUser;
    private final PoliticaAcceso politica;

    public UsuarioPropioConAcceso(GetUserUseCase getUser, UpdateUserUseCase updateUser, PoliticaAcceso politica) {
        this.getUser = getUser;
        this.updateUser = updateUser;
        this.politica = politica;
    }

    @Override
    public Optional<User> getUser(Integer id) {
        verificarPropio(id);
        return getUser.getUser(id);
    }

    @Override
    public Optional<User> updateUser(User datos) {
        verificarPropio(datos == null ? null : datos.getId());
        return updateUser.updateUser(datos);
    }

    @Override
    public User perfilActual() {
        Integer id = politica.requerido().id();
        return getUser.getUser(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
    }

    private void verificarPropio(Integer id) {
        UsuarioActual usuario = politica.requerido();
        if (!usuario.es(Role.ADMIN) && !Objects.equals(usuario.id(), id)) {
            throw new AccesoDenegadoException("Solo puede consultar o modificar sus propios datos");
        }
    }
}
