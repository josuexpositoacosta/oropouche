package com.oropouche.tasks.infrastructure.adapters.caso;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.infrastructure.entities.caso.CasoEntity;
import com.oropouche.tasks.infrastructure.repositories.caso.JpaCasoRepository;

/**
 * Adaptador de salida: persiste el caso con sus resultados e historial en una sola transacción.
 * Las transacciones viven aquí (infraestructura) para que la capa de aplicación no dependa de Spring.
 */
@Repository
public class JpaCasoRepositoryAdapter implements CasoRepositoryPort {

    private final JpaCasoRepository repository;

    public JpaCasoRepositoryAdapter(JpaCasoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Caso save(Caso caso) {
        return repository.save(CasoEntity.fromDomainModel(caso)).toDomainModel();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Caso> findById(Long id) {
        return repository.findById(id).map(CasoEntity::toDomainModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Caso> findAll(Long areaSaludId, Clasificacion clasificacion, EstadoCaso estado) {
        return repository.buscar(areaSaludId, clasificacion, estado).stream()
                .map(CasoEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Caso> findByPacienteId(Long pacienteId) {
        return repository.findByPacienteIdOrderByFechaNotificacionDesc(pacienteId).stream()
                .map(CasoEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAbiertoByPacienteId(Long pacienteId) {
        return repository.existsByPacienteIdAndEstado(pacienteId, EstadoCaso.ABIERTO);
    }
}
