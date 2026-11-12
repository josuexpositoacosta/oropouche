package com.oropouche.tasks.infrastructure.entities.caso;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oropouche.tasks.domain.models.caso.Dictamen;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caso_dictamen")
@Getter
@Setter
@NoArgsConstructor
public class DictamenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "diagnostico_principal", nullable = false, length = 2000)
    private String diagnosticoPrincipal;

    @Column(name = "diagnosticos_diferenciales", length = 2000)
    private String diagnosticosDiferenciales;

    @Column(nullable = false, length = 2000)
    private String conclusion;

    @Column(nullable = false, length = 100)
    private String usuario;

    public static DictamenEntity fromDomainModel(Dictamen d) {
        DictamenEntity e = new DictamenEntity();
        e.setId(d.getId());
        e.setFecha(d.getFecha());
        e.setDiagnosticoPrincipal(d.getDiagnosticoPrincipal());
        e.setDiagnosticosDiferenciales(d.getDiagnosticosDiferenciales());
        e.setConclusion(d.getConclusion());
        e.setUsuario(d.getUsuario());
        return e;
    }

    public Dictamen toDomainModel() {
        return new Dictamen(id, fecha, diagnosticoPrincipal, diagnosticosDiferenciales, conclusion, usuario);
    }
}
