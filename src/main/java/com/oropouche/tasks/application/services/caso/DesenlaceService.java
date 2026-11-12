package com.oropouche.tasks.application.services.caso;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.ports.in.DesenlaceUseCase;
import com.oropouche.tasks.domain.ports.out.DesenlaceRepositoryPort;

/**
 * Catálogo de desenlaces. Los valores no se borran (los casos cerrados los referencian):
 * se renombran o se desactivan.
 */
public class DesenlaceService implements DesenlaceUseCase {

    private static final String PATRON_CODIGO = "[A-Z][A-Z0-9_]{1,39}";

    private final DesenlaceRepositoryPort repository;

    public DesenlaceService(DesenlaceRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Desenlace> listar(boolean soloActivos) {
        return repository.findAll().stream()
                .filter(d -> !soloActivos || d.isActivo())
                .collect(Collectors.toList());
    }

    @Override
    public Desenlace crear(String codigo, String nombre) {

        String codigoNormalizado = normalizarCodigo(codigo);
        String nombreLimpio = validarNombre(nombre);

        if (repository.findByCodigo(codigoNormalizado).isPresent()) {
            throw new RecursoDuplicadoException("Ya existe un desenlace con el código " + codigoNormalizado);
        }

        return repository.save(new Desenlace(null, codigoNormalizado, nombreLimpio, true));
    }

    @Override
    public Desenlace renombrar(Long id, String nombre) {
        Desenlace desenlace = obtener(id);
        desenlace.setNombre(validarNombre(nombre));
        return repository.save(desenlace);
    }

    @Override
    public Desenlace cambiarEstado(Long id, boolean activo) {
        Desenlace desenlace = obtener(id);
        desenlace.setActivo(activo);
        return repository.save(desenlace);
    }

    @Override
    public void inicializarSiVacio() {
        if (repository.count() == 0) {
            Desenlace.INICIALES.forEach(d ->
                    repository.save(new Desenlace(null, d.getCodigo(), d.getNombre(), true)));
        }
    }

    private Desenlace obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del desenlace es obligatorio");
        }
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el desenlace con ID: " + id));
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del desenlace es obligatorio");
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (!normalizado.matches(PATRON_CODIGO)) {
            throw new IllegalArgumentException(
                    "El código solo admite letras sin tilde, números y guiones bajos (2 a 40 caracteres)");
        }
        return normalizado;
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del desenlace es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 100) {
            throw new IllegalArgumentException("El nombre del desenlace admite como máximo 100 caracteres");
        }
        return limpio;
    }
}
