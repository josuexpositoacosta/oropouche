package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesenlaceRequest {

    private String codigo;
    private String nombre;
}
