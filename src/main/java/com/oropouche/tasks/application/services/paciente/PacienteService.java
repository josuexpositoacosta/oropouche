
package com.oropouche.tasks.application.services.paciente;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.in.PacienteUseCase;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PacienteService implements PacienteUseCase {

    private final PacienteRepositoryPort pacienteRepositoryPort;
    private final AreaSaludRepositoryPort areaSaludRepositoryPort;

    public PacienteService(
            PacienteRepositoryPort pacienteRepositoryPort,
            AreaSaludRepositoryPort areaSaludRepositoryPort) {

        this.pacienteRepositoryPort = pacienteRepositoryPort;
        this.areaSaludRepositoryPort = areaSaludRepositoryPort;
    }

    @Override
    public Paciente registrar(Paciente paciente) {

        validarDatos(paciente);
        validarAreaActiva(paciente.getAreaSaludId());

        if (pacienteRepositoryPort.existsByNumeroDocumento(
                paciente.getTipoDocumento(),
                paciente.getNumeroDocumento())) {
            throw new RecursoDuplicadoException(
                    "Ya existe un paciente con ese documento"
            );
        }

        paciente.setId(null);
        paciente.setFechaRegistro(LocalDateTime.now());
        paciente.setActivo(true);

        return pacienteRepositoryPort.save(paciente);
    }

    @Override
    public Paciente actualizar(Long id, Paciente paciente) {

        Paciente existente = buscarPorId(id);

        validarDatos(paciente);

        // Cambiar de área exige que la nueva esté activa; mantener la misma
        // permite editar pacientes de un área ya desactivada.
        if (!paciente.getAreaSaludId().equals(existente.getAreaSaludId())) {
            validarAreaActiva(paciente.getAreaSaludId());
        }

        Optional<Paciente> duplicado =
                pacienteRepositoryPort.findByNumeroDocumento(
                        paciente.getTipoDocumento(),
                        paciente.getNumeroDocumento()
                );

        if (duplicado.isPresent()
                && !duplicado.get().getId().equals(id)) {
            throw new RecursoDuplicadoException(
                    "El documento pertenece a otro paciente"
            );
        }

        paciente.setId(id);
        paciente.setFechaRegistro(existente.getFechaRegistro());
        paciente.setActivo(existente.isActivo());

        return pacienteRepositoryPort.save(paciente);
    }

    @Override
    public Paciente buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del paciente no es válido"
            );
        }

        return pacienteRepositoryPort.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el paciente con ID: " + id
                ));
    }

    @Override
    public List<Paciente> listarTodos() {
        return pacienteRepositoryPort.findAll();
    }

    @Override
    public List<Paciente> buscarPorAreaSalud(Long areaSaludId) {

        if (areaSaludId == null || areaSaludId <= 0) {
            throw new IllegalArgumentException(
                    "El área de salud es obligatoria"
            );
        }

        if (areaSaludRepositoryPort.findById(areaSaludId).isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No existe el área de salud con ID: " + areaSaludId
            );
        }

        return pacienteRepositoryPort.findByAreaSaludId(areaSaludId);
    }

    @Override
    public Paciente desactivar(Long id) {

        Paciente paciente = buscarPorId(id);

        paciente.setActivo(false);

        return pacienteRepositoryPort.save(paciente);
    }

    /**
     * Regla de negocio: un paciente solo puede asignarse a un área de salud existente y activa.
     */
    private void validarAreaActiva(Long areaSaludId) {

        AreaSalud area = areaSaludRepositoryPort.findById(areaSaludId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el área de salud con ID: " + areaSaludId
                ));

        if (!area.isActivo()) {
            throw new IllegalArgumentException(
                    "El área de salud " + area.getCodigo() + " está desactivada"
            );
        }
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
