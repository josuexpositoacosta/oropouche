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

import com.oropouche.tasks.domain.models.caso.EvolucionCaso;
import com.oropouche.tasks.domain.models.caso.Seguimiento;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_seguimiento")
@Getter
@Setter
@NoArgsConstructor
public class SeguimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EvolucionCaso evolucion;

    @Column(length = 2000)
    private String observaciones;

    @Column(name = "proximo_control")
    private LocalDate proximoControl;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static SeguimientoEntity fromDomainModel(Seguimiento s) {
        SeguimientoEntity e = new SeguimientoEntity();
        e.setId(s.getId());
        e.setFecha(s.getFecha());
        e.setEvolucion(s.getEvolucion());
        e.setObservaciones(s.getObservaciones());
        e.setProximoControl(s.getProximoControl());
        e.setUsuario(s.getUsuario());
        return e;
    }

    public Seguimiento toDomainModel() {
        return new Seguimiento(id, fecha, evolucion, observaciones, proximoControl, usuario);
    }
}
