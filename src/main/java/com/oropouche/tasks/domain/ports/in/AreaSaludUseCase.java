package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;

/**
 * Puerto de entrada: gestión de áreas de salud.
 */
public interface AreaSaludUseCase {

    AreaSalud registrar(AreaSalud areaSalud);

    AreaSalud buscarPorId(Long id);

    AreaSalud buscarPorCodigo(String codigo);

    List<AreaSalud> listarTodas();

    List<AreaSalud> listarActivas();

    AreaSalud actualizar(Long id, AreaSalud datos);

    AreaSalud desactivar(Long id);
}
