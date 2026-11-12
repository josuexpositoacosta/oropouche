package com.oropouche.tasks.infrastructure.repositories.caso;

import java.time.LocalDate;
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

    @Query("select c from CasoEntity c"
            + " where c.fechaInicioSintomas between :desde and :hasta"
            + " and (:areaSaludId is null or c.areaSaludId = :areaSaludId)"
            + " order by c.fechaInicioSintomas, c.id")
    List<CasoEntity> buscarPorInicioSintomas(
            @Param("areaSaludId") Long areaSaludId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("select c from CasoEntity c"
            + " where c.fechaNotificacion between :desde and :hasta"
            + " and (:areaSaludId is null or c.areaSaludId = :areaSaludId)"
            + " order by c.fechaNotificacion, c.id")
    List<CasoEntity> buscarPorNotificacion(
            @Param("areaSaludId") Long areaSaludId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
