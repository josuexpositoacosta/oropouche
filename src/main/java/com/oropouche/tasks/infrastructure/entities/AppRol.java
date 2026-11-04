
package com.oropouche.tasks.infrastructure.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "app_rol")
public class AppRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idapp_rol;

    private String descripcion;

    private String nombre;

    // Constructor requerido por JPA
    protected AppRol() {
    }

    // Constructor para crear roles desde la aplicación
    public AppRol(Long idapp_rol, String descripcion, String nombre) {
        this.idapp_rol = idapp_rol;
        this.descripcion = descripcion;
        this.nombre = nombre;
    }
}
