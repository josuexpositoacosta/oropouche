package com.oropouche.tasks.domain.models.caso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Evaluación clínica del médico. Registro inmutable: se corrige con una evaluación nueva. */
@Getter
@Setter
@NoArgsConstructor
public class EvaluacionClinica {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal temperatura;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private Integer tensionSistolica;
    private Integer tensionDiastolica;
    private Integer saturacionO2;
    private Set<SignoClinico> signosClinicos = EnumSet.noneOf(SignoClinico.class);
    private Set<SignoAlarmaDengue> signosAlarmaDengue = EnumSet.noneOf(SignoAlarmaDengue.class);
    private String observaciones;
    private String usuario;

    public void setSignosClinicos(Set<SignoClinico> signos) {
        this.signosClinicos = signos == null || signos.isEmpty()
                ? EnumSet.noneOf(SignoClinico.class) : EnumSet.copyOf(signos);
    }

    public void setSignosAlarmaDengue(Set<SignoAlarmaDengue> signos) {
        this.signosAlarmaDengue = signos == null || signos.isEmpty()
                ? EnumSet.noneOf(SignoAlarmaDengue.class) : EnumSet.copyOf(signos);
    }

    public boolean tieneSignosVitales() {
        return temperatura != null || frecuenciaCardiaca != null || frecuenciaRespiratoria != null
                || tensionSistolica != null || tensionDiastolica != null || saturacionO2 != null;
    }
}
