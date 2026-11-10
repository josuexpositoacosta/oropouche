
package com.oropouche.tasks.infrastructure.repositories.area_salud;

import com.oropouche.tasks.infrastructure.entities.AreaSaludEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAreaSaludRepository
        extends JpaRepository<AreaSaludEntity, Long> {

    Optional<AreaSaludEntity> findByCodigo(String codigo);

    List<AreaSaludEntity> findByActivoTrue();

    boolean existsByCodigo(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);
}
