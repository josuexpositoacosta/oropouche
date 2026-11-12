package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entrada del historial de clasificación de un caso. {@code anterior} es nula en el registro inicial. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambioClasificacion {

    private Long id;
    private LocalDateTime fecha;
    private Clasificacion anterior;
    private Clasificacion nueva;
    private String motivo;
    private String usuario;
}
