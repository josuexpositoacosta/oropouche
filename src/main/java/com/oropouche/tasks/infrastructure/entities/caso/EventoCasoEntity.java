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

import com.oropouche.tasks.domain.models.caso.EventoCaso;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_evento")
@Getter
@Setter
@NoArgsConstructor
public class EventoCasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEventoCaso tipo;

    @Column(nullable = false, length = 2500)
    private String detalle;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static EventoCasoEntity fromDomainModel(EventoCaso ev) {
        EventoCasoEntity e = new EventoCasoEntity();
        e.setId(ev.getId());
        e.setFecha(ev.getFecha());
        e.setTipo(ev.getTipo());
        e.setDetalle(ev.getDetalle());
        e.setUsuario(ev.getUsuario());
        return e;
    }

    public EventoCaso toDomainModel() {
        return new EventoCaso(id, fecha, tipo, detalle, usuario);
    }
}
