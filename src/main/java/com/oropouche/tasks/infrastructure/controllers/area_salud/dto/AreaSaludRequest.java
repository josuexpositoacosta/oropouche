package com.oropouche.tasks.infrastructure.controllers.area_salud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaSaludRequest {

    private String codigo;
    private String nombre;
    private String provincia;
    private String municipio;
    private String direccion;
    private String telefono;
}
