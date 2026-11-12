package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.CambioClasificacion;
import com.oropouche.tasks.domain.models.caso.Clasificacion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_historial_clasificacion")
@Getter
@Setter
@NoArgsConstructor
public class CambioClasificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Clasificacion anterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Clasificacion nueva;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static CambioClasificacionEntity fromDomainModel(CambioClasificacion c) {
        CambioClasificacionEntity e = new CambioClasificacionEntity();
        e.setId(c.getId());
        e.setFecha(c.getFecha());
        e.setAnterior(c.getAnterior());
        e.setNueva(c.getNueva());
        e.setMotivo(c.getMotivo());
        e.setUsuario(c.getUsuario());
        return e;
    }

    public CambioClasificacion toDomainModel() {
        return new CambioClasificacion(id, fecha, anterior, nueva, motivo, usuario);
    }
}
