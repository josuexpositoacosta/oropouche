package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.caso.Desenlace;

/**
 * Puerto de entrada: catálogo editable de desenlaces de cierre de caso.
 */
public interface DesenlaceUseCase {

    List<Desenlace> listar(boolean soloActivos);

    Desenlace crear(String codigo, String nombre);

    Desenlace renombrar(Long id, String nombre);

    Desenlace cambiarEstado(Long id, boolean activo);

    /** Inserta los desenlaces iniciales si el catálogo está vacío. */
    void inicializarSiVacio();
}
