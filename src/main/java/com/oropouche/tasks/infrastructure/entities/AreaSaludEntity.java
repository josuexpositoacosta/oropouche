
package com.oropouche.tasks.infrastructure.entities;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "area_salud",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_area_salud_codigo",
            columnNames = "codigo"
        )
    }
)
public class AreaSaludEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public AreaSaludEntity() {
    }

    public AreaSalud toDomainModel() {

        return new AreaSalud(
            id,
            codigo,
            nombre,
            provincia,
            municipio,
            direccion,
            telefono,
            activo,
            fechaRegistro
        );
    }

    public static AreaSaludEntity fromDomainModel(
            AreaSalud areaSalud) {

        AreaSaludEntity entity = new AreaSaludEntity();

        entity.setId(areaSalud.getId());
        entity.setCodigo(areaSalud.getCodigo());
        entity.setNombre(areaSalud.getNombre());
        entity.setProvincia(areaSalud.getProvincia());
        entity.setMunicipio(areaSalud.getMunicipio());
        entity.setDireccion(areaSalud.getDireccion());
        entity.setTelefono(areaSalud.getTelefono());
        entity.setActivo(areaSalud.isActivo());
        entity.setFechaRegistro(areaSalud.getFechaRegistro());

        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
