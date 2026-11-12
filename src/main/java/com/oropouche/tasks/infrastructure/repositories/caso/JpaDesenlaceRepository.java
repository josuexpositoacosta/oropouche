package com.oropouche.tasks.infrastructure.repositories.caso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oropouche.tasks.infrastructure.entities.caso.DesenlaceEntity;

public interface JpaDesenlaceRepository extends JpaRepository<DesenlaceEntity, Long> {

    Optional<DesenlaceEntity> findByCodigo(String codigo);

    List<DesenlaceEntity> findAllByOrderByIdAsc();
}
