package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "caso",
    indexes = {
        @Index(name = "idx_caso_paciente", columnList = "paciente_id"),
        @Index(name = "idx_caso_area_salud", columnList = "area_salud_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "area_salud_id", nullable = false)
    private Long areaSaludId;

    @Column(name = "fecha_inicio_sintomas", nullable = false)
    private LocalDate fechaInicioSintomas;

    @Column(name = "fecha_notificacion", nullable = false)
    private LocalDate fechaNotificacion;

    @Column(nullable = false)
    private boolean fiebre;

    @Column(name = "cefalea_intensa", nullable = false)
    private boolean cefaleaIntensa;

    @ElementCollection
    @CollectionTable(name = "caso_manifestacion", joinColumns = @JoinColumn(name = "caso_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "manifestacion", length = 30)
    private Set<Manifestacion> manifestaciones = new HashSet<>();

    @Column(nullable = false)
    private boolean recaida;

    @Column(nullable = false)
    private boolean embarazada;

    @Column(name = "semanas_gestacion")
    private Integer semanasGestacion;

    @Column(name = "area_transmision_o_viaje", nullable = false)
    private boolean areaTransmisionOViaje;

    @Column(name = "detalle_area_transmision", length = 255)
    private String detalleAreaTransmision;

    @Column(name = "nexo_caso_confirmado", nullable = false)
    private boolean nexoCasoConfirmado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Clasificacion clasificacion;

    @Column(name = "clasificacion_validada", nullable = false)
    private boolean clasificacionValidada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCaso estado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<ResultadoLaboratorioEntity> resultados = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<CambioClasificacionEntity> historial = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<EvaluacionClinicaEntity> evaluaciones = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<DictamenEntity> dictamenes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<PlanAtencionEntity> planes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<SeguimientoEntity> seguimientos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "caso_id", nullable = false)
    @OrderBy("id")
    private List<EventoCasoEntity> eventos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "desenlace_id")
    private DesenlaceEntity desenlace;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "observaciones_cierre", length = 2000)
    private String observacionesCierre;

    public static CasoEntity fromDomainModel(Caso caso) {
        CasoEntity e = new CasoEntity();
        e.setId(caso.getId());
        e.setPacienteId(caso.getPacienteId());
        e.setAreaSaludId(caso.getAreaSaludId());
        e.setFechaInicioSintomas(caso.getFechaInicioSintomas());
        e.setFechaNotificacion(caso.getFechaNotificacion());
        e.setFiebre(caso.isFiebre());
        e.setCefaleaIntensa(caso.isCefaleaIntensa());
        e.setManifestaciones(new HashSet<>(caso.getManifestaciones()));
        e.setRecaida(caso.isRecaida());
        e.setEmbarazada(caso.isEmbarazada());
        e.setSemanasGestacion(caso.getSemanasGestacion());
        e.setAreaTransmisionOViaje(caso.isAreaTransmisionOViaje());
        e.setDetalleAreaTransmision(caso.getDetalleAreaTransmision());
        e.setNexoCasoConfirmado(caso.isNexoCasoConfirmado());
        e.setClasificacion(caso.getClasificacion());
        e.setClasificacionValidada(caso.isClasificacionValidada());
        e.setEstado(caso.getEstado());
        e.setResultados(caso.getResultados().stream()
                .map(ResultadoLaboratorioEntity::fromDomainModel)
                .collect(Collectors.toCollection(ArrayList::new)));
        e.setHistorial(caso.getHistorial().stream()
                .map(CambioClasificacionEntity::fromDomainModel)
                .collect(Collectors.toCollection(ArrayList::new)));
        e.setEvaluaciones(mapear(caso.getEvaluaciones(), EvaluacionClinicaEntity::fromDomainModel));
        e.setDictamenes(mapear(caso.getDictamenes(), DictamenEntity::fromDomainModel));
        e.setPlanes(mapear(caso.getPlanes(), PlanAtencionEntity::fromDomainModel));
        e.setSeguimientos(mapear(caso.getSeguimientos(), SeguimientoEntity::fromDomainModel));
        e.setEventos(mapear(caso.getEventos(), EventoCasoEntity::fromDomainModel));
        e.setDesenlace(caso.getDesenlace() == null ? null : DesenlaceEntity.fromDomainModel(caso.getDesenlace()));
        e.setFechaCierre(caso.getFechaCierre());
        e.setObservacionesCierre(caso.getObservacionesCierre());
        return e;
    }

    public Caso toDomainModel() {
        Caso caso = new Caso();
        caso.setId(id);
        caso.setPacienteId(pacienteId);
        caso.setAreaSaludId(areaSaludId);
        caso.setFechaInicioSintomas(fechaInicioSintomas);
        caso.setFechaNotificacion(fechaNotificacion);
        caso.setFiebre(fiebre);
        caso.setCefaleaIntensa(cefaleaIntensa);
        caso.setManifestaciones(manifestaciones.isEmpty()
                ? EnumSet.noneOf(Manifestacion.class)
                : EnumSet.copyOf(manifestaciones));
        caso.setRecaida(recaida);
        caso.setEmbarazada(embarazada);
        caso.setSemanasGestacion(semanasGestacion);
        caso.setAreaTransmisionOViaje(areaTransmisionOViaje);
        caso.setDetalleAreaTransmision(detalleAreaTransmision);
        caso.setNexoCasoConfirmado(nexoCasoConfirmado);
        caso.setClasificacion(clasificacion);
        caso.setClasificacionValidada(clasificacionValidada);
        caso.setEstado(estado);
        caso.setResultados(resultados.stream()
                .map(ResultadoLaboratorioEntity::toDomainModel)
                .collect(Collectors.toList()));
        caso.setHistorial(historial.stream()
                .map(CambioClasificacionEntity::toDomainModel)
                .collect(Collectors.toList()));
        caso.setEvaluaciones(mapear(evaluaciones, EvaluacionClinicaEntity::toDomainModel));
        caso.setDictamenes(mapear(dictamenes, DictamenEntity::toDomainModel));
        caso.setPlanes(mapear(planes, PlanAtencionEntity::toDomainModel));
        caso.setSeguimientos(mapear(seguimientos, SeguimientoEntity::toDomainModel));
        caso.setEventos(mapear(eventos, EventoCasoEntity::toDomainModel));
        caso.setDesenlace(desenlace == null ? null : desenlace.toDomainModel());
        caso.setFechaCierre(fechaCierre);
        caso.setObservacionesCierre(observacionesCierre);
        return caso;
    }

    private static <A, B> List<B> mapear(List<A> origen, Function<A, B> funcion) {
        return origen.stream().map(funcion).collect(Collectors.toCollection(ArrayList::new));
    }
}
