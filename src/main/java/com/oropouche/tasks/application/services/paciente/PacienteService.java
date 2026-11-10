
package com.oropouche.tasks.application.services.paciente;

import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PacienteService {

    private final PacienteRepositoryPort pacienteRepositoryPort;

    public PacienteService(PacienteRepositoryPort pacienteRepositoryPort) {
        this.pacienteRepositoryPort = pacienteRepositoryPort;
    }

    public Paciente registrar(Paciente paciente) {

        validarDatos(paciente);

        if (pacienteRepositoryPort.existsByNumeroDocumento(
                paciente.getTipoDocumento(),
                paciente.getNumeroDocumento())) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente con ese documento"
            );
        }

        paciente.setId(null);
        paciente.setFechaRegistro(LocalDateTime.now());
        paciente.setActivo(true);

        return pacienteRepositoryPort.save(paciente);
    }

    public Paciente actualizar(Long id, Paciente paciente) {

        Paciente existente = pacienteRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el paciente con ID: " + id
                ));

        validarDatos(paciente);

        Optional<Paciente> duplicado =
                pacienteRepositoryPort.findByNumeroDocumento(
                        paciente.getTipoDocumento(),
                        paciente.getNumeroDocumento()
                );

        if (duplicado.isPresent()
                && !duplicado.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "El documento pertenece a otro paciente"
            );
        }

        paciente.setId(id);
        paciente.setFechaRegistro(existente.getFechaRegistro());
        paciente.setActivo(existente.isActivo());

        return pacienteRepositoryPort.save(paciente);
    }

    public Paciente buscarPorId(Long id) {
        return pacienteRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el paciente con ID: " + id
                ));
    }

    public List<Paciente> buscarPorAreaSalud(Long areaSaludId) {

        if (areaSaludId == null || areaSaludId <= 0) {
            throw new IllegalArgumentException(
                    "El área de salud es obligatoria"
            );
        }

        return pacienteRepositoryPort.findByAreaSaludId(areaSaludId);
    }

    public Paciente desactivar(Long id) {

        Paciente paciente = buscarPorId(id);

        paciente.setActivo(false);

        return pacienteRepositoryPort.save(paciente);
    }

    private void validarDatos(Paciente paciente) {

        if (paciente == null) {
            throw new IllegalArgumentException(
                    "Los datos del paciente son obligatorios"
            );
        }

        validarTexto(paciente.getTipoDocumento(), "Tipo de documento");
        validarTexto(paciente.getNumeroDocumento(), "Número de documento");
        validarTexto(paciente.getNombres(), "Nombres");
        validarTexto(paciente.getApellidos(), "Apellidos");
        validarTexto(paciente.getSexo(), "Sexo");

        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria"
            );
        }

        if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura"
            );
        }

        if (paciente.getAreaSaludId() == null
                || paciente.getAreaSaludId() <= 0) {
            throw new IllegalArgumentException(
                    "El paciente debe pertenecer a un área de salud válida"
            );
        }

        paciente.setTipoDocumento(
                paciente.getTipoDocumento().trim()
        );
        paciente.setNumeroDocumento(
                paciente.getNumeroDocumento().trim()
        );
        paciente.setNombres(
                paciente.getNombres().trim()
        );
        paciente.setApellidos(
                paciente.getApellidos().trim()
        );
        paciente.setSexo(
                paciente.getSexo().trim()
        );
    }

    private void validarTexto(String valor, String campo) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio"
            );
        }
    }
}
