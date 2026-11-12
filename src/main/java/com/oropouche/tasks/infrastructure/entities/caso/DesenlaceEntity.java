package com.oropouche.tasks.infrastructure.entities.caso;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.oropouche.tasks.domain.models.caso.Desenlace;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "desenlace", uniqueConstraints = @UniqueConstraint(name = "uk_desenlace_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class DesenlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;

    public static DesenlaceEntity fromDomainModel(Desenlace d) {
        DesenlaceEntity e = new DesenlaceEntity();
        e.setId(d.getId());
        e.setCodigo(d.getCodigo());
        e.setNombre(d.getNombre());
        e.setActivo(d.isActivo());
        return e;
    }

    public Desenlace toDomainModel() {
        return new Desenlace(id, codigo, nombre, activo);
    }
}
