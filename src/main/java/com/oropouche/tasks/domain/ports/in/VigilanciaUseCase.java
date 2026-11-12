package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.vigilancia.ConteoArea;
import com.oropouche.tasks.domain.models.vigilancia.ConteoSemana;
import com.oropouche.tasks.domain.models.vigilancia.FiltroVigilancia;
import com.oropouche.tasks.domain.models.vigilancia.ResumenVigilancia;

/**
 * Puerto de entrada: estadísticas de vigilancia epidemiológica. Los campos nulos del filtro toman
 * valores por defecto: últimas 12 semanas, todas las áreas, fecha de inicio de síntomas.
 */
public interface VigilanciaUseCase {

    ResumenVigilancia resumen(FiltroVigilancia filtro);

    /** Curva epidémica: una entrada por semana del periodo, incluidas las semanas sin casos. */
    List<ConteoSemana> curvaEpidemica(FiltroVigilancia filtro);

    /** Una entrada por área de salud, incluidas las áreas sin casos. */
    List<ConteoArea> porArea(FiltroVigilancia filtro);

    /** Casos del periodo para exportar (línea de casos). */
    List<Caso> lineaDeCasos(FiltroVigilancia filtro);

    /** El filtro con los valores por defecto aplicados y validado. */
    FiltroVigilancia normalizar(FiltroVigilancia filtro);
}
