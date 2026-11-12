package com.oropouche.tasks.application.services.acceso;

import java.util.Arrays;
import java.util.Objects;

import com.oropouche.tasks.domain.exceptions.AccesoDenegadoException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

/**
 * Reglas de acceso por rol y área de salud:
 * <ul>
 *   <li>MEDICO: lee y escribe datos clínicos solo de su área.</li>
 *   <li>EPIDEMIOLOGO: lee datos clínicos de todas las áreas; no registra atención clínica.</li>
 *   <li>ADMIN: no accede a datos clínicos.</li>
 * </ul>
 */
public class PoliticaAcceso {

    private final UsuarioActualPort usuarioActualPort;

    public PoliticaAcceso(UsuarioActualPort usuarioActualPort) {
        this.usuarioActualPort = usuarioActualPort;
    }

    public UsuarioActual requerido() {
        return usuarioActualPort.actual()
                .orElseThrow(() -> new AccesoDenegadoException("Se requiere un usuario autenticado"));
    }

    public UsuarioActual verificarRol(Role... roles) {
        UsuarioActual usuario = requerido();
        if (Arrays.stream(roles).noneMatch(usuario::es)) {
            throw new AccesoDenegadoException("Su rol no permite esta operación");
        }
        return usuario;
    }

    public boolean puedeLeer(Long areaSaludId) {
        UsuarioActual usuario = requerido();
        return usuario.es(Role.EPIDEMIOLOGO)
                || (usuario.es(Role.MEDICO) && Objects.equals(usuario.areaSaludId(), areaSaludId));
    }

    public void verificarLectura(Long areaSaludId) {
        UsuarioActual usuario = verificarRol(Role.MEDICO, Role.EPIDEMIOLOGO);
        if (!puedeLeer(areaSaludId)) {
            throw denegadoPorArea(usuario);
        }
    }

    public void verificarEscritura(Long areaSaludId) {
        UsuarioActual usuario = verificarRol(Role.MEDICO);
        if (!Objects.equals(usuario.areaSaludId(), areaSaludId)) {
            throw denegadoPorArea(usuario);
        }
    }

    /**
     * Área por la que filtrar un listado: el epidemiólogo puede pedir cualquiera (o todas, con null);
     * el médico siempre la suya.
     */
    public Long areaParaListar(Long areaSolicitada) {
        UsuarioActual usuario = verificarRol(Role.MEDICO, Role.EPIDEMIOLOGO);
        if (usuario.es(Role.EPIDEMIOLOGO)) {
            return areaSolicitada;
        }
        if (areaSolicitada != null && !areaSolicitada.equals(usuario.areaSaludId())) {
            throw denegadoPorArea(usuario);
        }
        return usuario.areaSaludId();
    }

    private static AccesoDenegadoException denegadoPorArea(UsuarioActual usuario) {
        return new AccesoDenegadoException(usuario.es(Role.MEDICO)
                ? "Solo puede acceder a datos de su área de salud"
                : "Su rol no permite esta operación");
    }
}
