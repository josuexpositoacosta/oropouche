package com.oropouche.tasks.infrastructure.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.oropouche.tasks.domain.models.paciente.Paciente;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "paciente",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_paciente_documento",
        columnNames = {"tipo_documento", "numero_documento"}
    ),
    indexes = @Index(name = "idx_paciente_area_salud", columnList = "area_salud_id")
)
@Getter
@Setter
@NoArgsConstructor
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 30)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 50)
    private String numeroDocumento;

    @Column(nullable = false, length = 150)
    private String nombres;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 20)
    private String sexo;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(name = "area_salud_id", nullable = false)
    private Long areaSaludId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean activo;

    public static PacienteEntity fromDomainModel(Paciente paciente) {

        PacienteEntity entity = new PacienteEntity();
        entity.setId(paciente.getId());
        entity.setTipoDocumento(paciente.getTipoDocumento());
        entity.setNumeroDocumento(paciente.getNumeroDocumento());
        entity.setNombres(paciente.getNombres());
        entity.setApellidos(paciente.getApellidos());
        entity.setFechaNacimiento(paciente.getFechaNacimiento());
        entity.setSexo(paciente.getSexo());
        entity.setDireccion(paciente.getDireccion());
        entity.setTelefono(paciente.getTelefono());
        entity.setAreaSaludId(paciente.getAreaSaludId());
        entity.setFechaRegistro(paciente.getFechaRegistro());
        entity.setActivo(paciente.isActivo());
        return entity;
    }

    public Paciente toDomainModel() {

        return new Paciente(
                id,
                tipoDocumento,
                numeroDocumento,
                nombres,
                apellidos,
                fechaNacimiento,
                sexo,
                direccion,
                telefono,
                areaSaludId,
                fechaRegistro,
                activo
        );
    }
}
