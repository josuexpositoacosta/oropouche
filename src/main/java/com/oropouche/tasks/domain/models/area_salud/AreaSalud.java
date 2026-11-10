
package com.oropouche.tasks.domain.models.area_salud;

import java.time.LocalDateTime;

public class AreaSalud {

    private Long id;
    private String codigo;
    private String nombre;
    private String provincia;
    private String municipio;
    private String direccion;
    private String telefono;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public AreaSalud() {
    }

    public AreaSalud(
            Long id,
            String codigo,
            String nombre,
            String provincia,
            String municipio,
            String direccion,
            String telefono,
            boolean activo,
            LocalDateTime fechaRegistro) {

        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.provincia = provincia;
        this.municipio = municipio;
        this.direccion = direccion;
        this.telefono = telefono;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
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
