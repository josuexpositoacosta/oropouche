package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cierre o reapertura de un caso, con su detalle y el usuario que lo hizo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoCaso {

    private Long id;
    private LocalDateTime fecha;
    private TipoEventoCaso tipo;
    private String detalle;
    private String usuario;
}
