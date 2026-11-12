package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resultado_laboratorio")
@Getter
@Setter
@NoArgsConstructor
public class ResultadoLaboratorioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgenteEtiologico agente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_prueba", nullable = false, length = 30)
    private TipoPrueba tipoPrueba;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_muestra", nullable = false, length = 20)
    private TipoMuestra tipoMuestra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoPrueba resultado;

    @Column(name = "fecha_toma", nullable = false)
    private LocalDate fechaToma;

    @Column(name = "fecha_resultado")
    private LocalDate fechaResultado;

    public static ResultadoLaboratorioEntity fromDomainModel(ResultadoLaboratorio r) {
        ResultadoLaboratorioEntity e = new ResultadoLaboratorioEntity();
        e.setId(r.getId());
        e.setAgente(r.getAgente());
        e.setTipoPrueba(r.getTipoPrueba());
        e.setTipoMuestra(r.getTipoMuestra());
        e.setResultado(r.getResultado());
        e.setFechaToma(r.getFechaToma());
        e.setFechaResultado(r.getFechaResultado());
        return e;
    }

    public ResultadoLaboratorio toDomainModel() {
        return new ResultadoLaboratorio(id, agente, tipoPrueba, tipoMuestra, resultado, fechaToma, fechaResultado);
    }
}
