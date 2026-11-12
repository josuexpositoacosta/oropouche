package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoLaboratorio {

    private Long id;
    private AgenteEtiologico agente;
    private TipoPrueba tipoPrueba;
    private TipoMuestra tipoMuestra;
    private ResultadoPrueba resultado;
    private LocalDate fechaToma;
    private LocalDate fechaResultado;

    public boolean esPositivoDe(AgenteEtiologico agenteBuscado, TipoPrueba tipo) {
        return agente == agenteBuscado && tipoPrueba == tipo && resultado == ResultadoPrueba.POSITIVO;
    }
}
