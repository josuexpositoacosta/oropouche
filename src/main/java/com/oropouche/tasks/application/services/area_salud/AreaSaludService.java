
package com.oropouche.tasks.application.services.area_salud;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.ports.in.AreaSaludUseCase;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

public class AreaSaludService implements AreaSaludUseCase {

    private final AreaSaludRepositoryPort areaSaludRepositoryPort;

    public AreaSaludService(
            AreaSaludRepositoryPort areaSaludRepositoryPort) {
        this.areaSaludRepositoryPort = areaSaludRepositoryPort;
    }

    @Override
    public AreaSalud registrar(AreaSalud areaSalud) {

        validarDatos(areaSalud);

        String codigo = areaSalud.getCodigo().trim();
        String nombre = areaSalud.getNombre().trim();

        if (areaSaludRepositoryPort.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un área con ese código"
            );
        }

        if (areaSaludRepositoryPort.existsByNombre(nombre)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un área con ese nombre"
            );
        }

        areaSalud.setId(null);
        areaSalud.setCodigo(codigo);
        areaSalud.setNombre(nombre);
        areaSalud.setActivo(true);
        areaSalud.setFechaRegistro(LocalDateTime.now());

        return areaSaludRepositoryPort.save(areaSalud);
    }

    @Override
    public AreaSalud buscarPorId(Long id) {

        validarId(id);

        return areaSaludRepositoryPort.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el área de salud con ID: " + id
                ));
    }

    @Override
    public AreaSalud buscarPorCodigo(String codigo) {

        validarTexto(codigo, "Código");

        return areaSaludRepositoryPort.findByCodigo(codigo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un área con ese código"
                ));
    }

    @Override
    public List<AreaSalud> listarTodas() {
        return areaSaludRepositoryPort.findAll();
    }

    @Override
    public List<AreaSalud> listarActivas() {
        return areaSaludRepositoryPort.findAllActivas();
    }

    @Override
    public AreaSalud actualizar(Long id, AreaSalud datos) {

        AreaSalud existente = buscarPorId(id);

        validarDatos(datos);

        String codigo = datos.getCodigo().trim();
        String nombre = datos.getNombre().trim();

        areaSaludRepositoryPort.findByCodigo(codigo)
                .filter(area -> !area.getId().equals(id))
                .ifPresent(area -> {
                    throw new RecursoDuplicadoException(
                            "El código pertenece a otra área"
                    );
                });

       areaSaludRepositoryPort.findAll().stream()
        .filter(area -> area.getNombre().equalsIgnoreCase(nombre))
        .filter(area -> !area.getId().equals(id))
        .findFirst()
        .ifPresent(area -> {
            throw new RecursoDuplicadoException(
                    "El nombre pertenece a otra área"
            );
        });

        existente.setCodigo(codigo);
        existente.setNombre(nombre);
        existente.setProvincia(datos.getProvincia().trim());
        existente.setMunicipio(datos.getMunicipio().trim());
        existente.setDireccion(datos.getDireccion());
        existente.setTelefono(datos.getTelefono());

        return areaSaludRepositoryPort.save(existente);
    }

    @Override
    public AreaSalud desactivar(Long id) {

        AreaSalud area = buscarPorId(id);

        area.setActivo(false);

        return areaSaludRepositoryPort.save(area);
    }

    private void validarDatos(AreaSalud area) {

        if (area == null) {
            throw new IllegalArgumentException(
                    "Los datos del área son obligatorios"
            );
        }

        validarTexto(area.getCodigo(), "Código");
        validarTexto(area.getNombre(), "Nombre");
        validarTexto(area.getProvincia(), "Provincia");
        validarTexto(area.getMunicipio(), "Municipio");
    }

    private void validarTexto(String valor, String campo) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio"
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del área no es válido"
            );
        }
    }
}
