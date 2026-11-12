package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Plan de atención. Registro inmutable: un plan nuevo sustituye al anterior como vigente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanAtencion {

    private Long id;
    private LocalDateTime fecha;
    private ModalidadAtencion modalidad;
    private String indicaciones;
    private LocalDate proximoControl;
    private String usuario;
}
