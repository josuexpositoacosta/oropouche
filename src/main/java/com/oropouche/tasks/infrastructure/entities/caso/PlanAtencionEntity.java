package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_plan_atencion")
@Getter
@Setter
@NoArgsConstructor
public class PlanAtencionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ModalidadAtencion modalidad;

    @Column(nullable = false, length = 2000)
    private String indicaciones;

    @Column(name = "proximo_control")
    private LocalDate proximoControl;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static PlanAtencionEntity fromDomainModel(PlanAtencion p) {
        PlanAtencionEntity e = new PlanAtencionEntity();
        e.setId(p.getId());
        e.setFecha(p.getFecha());
        e.setModalidad(p.getModalidad());
        e.setIndicaciones(p.getIndicaciones());
        e.setProximoControl(p.getProximoControl());
        e.setUsuario(p.getUsuario());
        return e;
    }

    public PlanAtencion toDomainModel() {
        return new PlanAtencion(id, fecha, modalidad, indicaciones, proximoControl, usuario);
    }
}
