package com.oropouche.tasks.infrastructure.entities.caso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.SignoAlarmaDengue;
import com.oropouche.tasks.domain.models.caso.SignoClinico;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_evaluacion_clinica")
@Getter
@Setter
@NoArgsConstructor
public class EvaluacionClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    @Column(name = "tension_sistolica")
    private Integer tensionSistolica;

    @Column(name = "tension_diastolica")
    private Integer tensionDiastolica;

    @Column(name = "saturacion_o2")
    private Integer saturacionO2;

    @ElementCollection
    @CollectionTable(name = "evaluacion_signo_clinico", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "signo", length = 30)
    private Set<SignoClinico> signosClinicos = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "evaluacion_signo_alarma_dengue", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "signo", length = 40)
    private Set<SignoAlarmaDengue> signosAlarmaDengue = new HashSet<>();

    @Column(length = 2000)
    private String observaciones;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static EvaluacionClinicaEntity fromDomainModel(EvaluacionClinica ev) {
        EvaluacionClinicaEntity e = new EvaluacionClinicaEntity();
        e.setId(ev.getId());
        e.setFecha(ev.getFecha());
        e.setTemperatura(ev.getTemperatura());
        e.setFrecuenciaCardiaca(ev.getFrecuenciaCardiaca());
        e.setFrecuenciaRespiratoria(ev.getFrecuenciaRespiratoria());
        e.setTensionSistolica(ev.getTensionSistolica());
        e.setTensionDiastolica(ev.getTensionDiastolica());
        e.setSaturacionO2(ev.getSaturacionO2());
        e.setSignosClinicos(new HashSet<>(ev.getSignosClinicos()));
        e.setSignosAlarmaDengue(new HashSet<>(ev.getSignosAlarmaDengue()));
        e.setObservaciones(ev.getObservaciones());
        e.setUsuario(ev.getUsuario());
        return e;
    }

    public EvaluacionClinica toDomainModel() {
        EvaluacionClinica ev = new EvaluacionClinica();
        ev.setId(id);
        ev.setFecha(fecha);
        ev.setTemperatura(temperatura);
        ev.setFrecuenciaCardiaca(frecuenciaCardiaca);
        ev.setFrecuenciaRespiratoria(frecuenciaRespiratoria);
        ev.setTensionSistolica(tensionSistolica);
        ev.setTensionDiastolica(tensionDiastolica);
        ev.setSaturacionO2(saturacionO2);
        ev.setSignosClinicos(signosClinicos.isEmpty() ? EnumSet.noneOf(SignoClinico.class) : EnumSet.copyOf(signosClinicos));
        ev.setSignosAlarmaDengue(signosAlarmaDengue.isEmpty()
                ? EnumSet.noneOf(SignoAlarmaDengue.class) : EnumSet.copyOf(signosAlarmaDengue));
        ev.setObservaciones(observaciones);
        ev.setUsuario(usuario);
        return ev;
    }
}
