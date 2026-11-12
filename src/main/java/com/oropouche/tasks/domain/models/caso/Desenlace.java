package com.oropouche.tasks.domain.models.caso;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Desenlace con el que se cierra un caso. Es un catálogo editable: se pueden añadir, renombrar y
 * desactivar valores. No se borran, porque los casos cerrados los referencian.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Desenlace {

    /** Valores con los que se inicializa el catálogo si está vacío. */
    public static final List<Desenlace> INICIALES = List.of(
            new Desenlace(null, "RECUPERADO", "Recuperado", true),
            new Desenlace(null, "RECUPERADO_CON_SECUELAS", "Recuperado con secuelas", true),
            new Desenlace(null, "FALLECIDO", "Fallecido", true),
            new Desenlace(null, "TRASLADADO", "Trasladado", true),
            new Desenlace(null, "PERDIDO_SEGUIMIENTO", "Perdido en el seguimiento", true));

    private Long id;
    private String codigo;
    private String nombre;
    private boolean activo;
}
