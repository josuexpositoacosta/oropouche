package com.oropouche.tasks.infrastructure.adapters.area_salud;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.infrastructure.entities.AreaSaludEntity;
import com.oropouche.tasks.infrastructure.repositories.area_salud.JpaAreaSaludRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaAreaSaludRepositoryAdapter implements AreaSaludRepositoryPort {

    private final JpaAreaSaludRepository repository;

    public JpaAreaSaludRepositoryAdapter(JpaAreaSaludRepository repository) {
        this.repository = repository;
    }

    @Override
    public AreaSalud save(AreaSalud areaSalud) {
        AreaSaludEntity entity = AreaSaludEntity.fromDomainModel(areaSalud);
        return repository.save(entity).toDomainModel();
    }

    @Override
    public Optional<AreaSalud> findById(Long id) {
        return repository.findById(id)
                .map(AreaSaludEntity::toDomainModel);
    }

    @Override
    public Optional<AreaSalud> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .map(AreaSaludEntity::toDomainModel);
    }

    @Override
    public List<AreaSalud> findAll() {
        return repository.findAll()
                .stream()
                .map(AreaSaludEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<AreaSalud> findAllActivas() {
        return repository.findByActivoTrue()
                .stream()
                .map(AreaSaludEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return repository.existsByNombreIgnoreCase(nombre);
    }
}