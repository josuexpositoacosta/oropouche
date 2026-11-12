package com.oropouche.tasks.infrastructure.adapters.caso;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.ports.out.DesenlaceRepositoryPort;
import com.oropouche.tasks.infrastructure.entities.caso.DesenlaceEntity;
import com.oropouche.tasks.infrastructure.repositories.caso.JpaDesenlaceRepository;

@Repository
public class JpaDesenlaceRepositoryAdapter implements DesenlaceRepositoryPort {

    private final JpaDesenlaceRepository repository;

    public JpaDesenlaceRepositoryAdapter(JpaDesenlaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Desenlace save(Desenlace desenlace) {
        return repository.save(DesenlaceEntity.fromDomainModel(desenlace)).toDomainModel();
    }

    @Override
    public Optional<Desenlace> findById(Long id) {
        return repository.findById(id).map(DesenlaceEntity::toDomainModel);
    }

    @Override
    public Optional<Desenlace> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(DesenlaceEntity::toDomainModel);
    }

    @Override
    public List<Desenlace> findAll() {
        return repository.findAllByOrderByIdAsc().stream()
                .map(DesenlaceEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return repository.count();
    }
}
