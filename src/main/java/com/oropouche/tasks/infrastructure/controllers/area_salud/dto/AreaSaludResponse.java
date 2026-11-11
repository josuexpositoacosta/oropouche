package com.oropouche.tasks.infrastructure.controllers.area_salud.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaSaludResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String provincia;
    private String municipio;
    private String direccion;
    private String telefono;
    private boolean activo;
    private LocalDateTime fechaRegistro;
}
