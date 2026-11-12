package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Control de seguimiento. Registro inmutable. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seguimiento {

    private Long id;
    private LocalDateTime fecha;
    private EvolucionCaso evolucion;
    private String observaciones;
    private LocalDate proximoControl;
    private String usuario;
}
