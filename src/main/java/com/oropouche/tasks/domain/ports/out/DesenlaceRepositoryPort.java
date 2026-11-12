package com.oropouche.tasks.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.oropouche.tasks.domain.models.caso.Desenlace;

public interface DesenlaceRepositoryPort {

    Desenlace save(Desenlace desenlace);

    Optional<Desenlace> findById(Long id);

    Optional<Desenlace> findByCodigo(String codigo);

    List<Desenlace> findAll();

    long count();
}
