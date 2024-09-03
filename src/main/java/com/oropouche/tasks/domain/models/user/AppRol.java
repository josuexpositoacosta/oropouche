package com.oropouche.tasks.domain.models.user;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppRol {

    private Long idapp_rol;
    private String descripcion;
    private String nombre;

    public AppRol(Long idapp_rol, String descripcion, String nombre) {
        this.idapp_rol = idapp_rol;
        this.descripcion = descripcion;
        this.nombre = nombre;
    }

}
