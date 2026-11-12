package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CierreRequest {

    /** Código del desenlace del catálogo (por ejemplo, RECUPERADO). */
    private String desenlace;
    private LocalDate fechaCierre;
    private String observaciones;
}
