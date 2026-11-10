
package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;

import java.util.List;
import java.util.Optional;

public interface AreaSaludRepositoryPort {

    AreaSalud save(AreaSalud areaSalud);

    Optional<AreaSalud> findById(Long id);

    Optional<AreaSalud> findByCodigo(String codigo);

    List<AreaSalud> findAll();

    List<AreaSalud> findAllActivas();

    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);
}
