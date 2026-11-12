package com.oropouche.tasks.infrastructure.repositories.caso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.infrastructure.entities.caso.CasoEntity;

public interface JpaCasoRepository extends JpaRepository<CasoEntity, Long> {

    @Query("select c from CasoEntity c"
            + " where (:areaSaludId is null or c.areaSaludId = :areaSaludId)"
            + " and (:clasificacion is null or c.clasificacion = :clasificacion)"
            + " and (:estado is null or c.estado = :estado)"
            + " order by c.fechaNotificacion desc, c.id desc")
    List<CasoEntity> buscar(
            @Param("areaSaludId") Long areaSaludId,
            @Param("clasificacion") Clasificacion clasificacion,
            @Param("estado") EstadoCaso estado);

    List<CasoEntity> findByPacienteIdOrderByFechaNotificacionDesc(Long pacienteId);

    boolean existsByPacienteIdAndEstado(Long pacienteId, EstadoCaso estado);
}
