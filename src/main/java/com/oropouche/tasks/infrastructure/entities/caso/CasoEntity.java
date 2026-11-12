package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        e.setEstado(caso.getEstado());
        e.setResultados(caso.getResultados().stream()
                .map(ResultadoLaboratorioEntity::fromDomainModel)
                .collect(Collectors.toCollection(ArrayList::new)));
        e.setHistorial(caso.getHistorial().stream()
                .map(CambioClasificacionEntity::fromDomainModel)
                .collect(Collectors.toCollection(ArrayList::new)));
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
        caso.setEstado(estado);
        caso.setResultados(resultados.stream()
                .map(ResultadoLaboratorioEntity::toDomainModel)
                .collect(Collectors.toList()));
        caso.setHistorial(historial.stream()
                .map(CambioClasificacionEntity::toDomainModel)
                .collect(Collectors.toList()));
        return caso;
    }
}
