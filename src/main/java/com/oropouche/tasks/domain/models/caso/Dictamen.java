package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dictamen médico. Registro inmutable. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dictamen {

    private Long id;
    private LocalDateTime fecha;
    private String diagnosticoPrincipal;
    private String diagnosticosDiferenciales;
    private String conclusion;
    private String usuario;
}
