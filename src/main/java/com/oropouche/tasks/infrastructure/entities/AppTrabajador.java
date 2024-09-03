package com.oropouche.tasks.infrastructure.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.security.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "app_trabajador")
public class AppTrabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idapp_trabajador;
    private String ci;
    private String clave;
    private String direccion;
    private Integer enabled;
    private String escolaridad;
    private Timestamp fechaRegistro;
    private String idestado;
    private String sexo;
    private String usuario;
    private Long app_area_idarea;
    private Long app_rol_idrol;
    private String apellido;
    private String nombre;


    public AppTrabajador(Long idapp_trabajador, String ci, String clave, String direccion, Integer enabled, String escolaridad, Timestamp fechaRegistro, String idestado, String sexo, String usuario, Long app_area_idarea, Long app_rol_idrol, String apellido, String nombre) {
        this.idapp_trabajador = idapp_trabajador;
        this.ci = ci;
        this.clave = clave;
        this.direccion = direccion;
        this.enabled = enabled;
        this.escolaridad = escolaridad;
        this.fechaRegistro = fechaRegistro;
        this.idestado = idestado;
        this.sexo = sexo;
        this.usuario = usuario;
        this.app_area_idarea = app_area_idarea;
        this.app_rol_idrol = app_rol_idrol;
        this.apellido = apellido;
        this.nombre = nombre;
    }

    // Getters y Setters


}
