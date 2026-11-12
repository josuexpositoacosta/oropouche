package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import com.oropouche.tasks.domain.models.caso.Desenlace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesenlaceResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private boolean activo;

    public static DesenlaceResponse from(Desenlace d) {
        return d == null ? null : new DesenlaceResponse(d.getId(), d.getCodigo(), d.getNombre(), d.isActivo());
    }
}
