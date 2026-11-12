package com.oropouche.tasks.application.services.user;

import java.util.List;
import java.util.Objects;

import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.exceptions.UserAlreadyExistsException;
import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.GestionUsuariosUseCase;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PasswordEncoderPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

/**
 * Alta y administración de usuarios. Sustituye al registro público: los usuarios los crea el ADMIN.
 * Java puro.
 */
public class GestionUsuariosService implements GestionUsuariosUseCase {

    static final int MIN_PASSWORD = 8;
    private static final String PATRON_USERNAME = "[A-Za-z0-9._-]{3,50}";

    private final UserRepositoryPort usuarios;
    private final PasswordEncoderPort passwordEncoder;
    private final AreaSaludRepositoryPort areas;
    private final UsuarioActualPort usuarioActual;

    public GestionUsuariosService(UserRepositoryPort usuarios, PasswordEncoderPort passwordEncoder,
                                  AreaSaludRepositoryPort areas, UsuarioActualPort usuarioActual) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.areas = areas;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public User crear(User nuevo) {

        if (nuevo == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }
        String username = validarUsername(nuevo.getUsername());
        validarPassword(nuevo.getPassword());
        requerido(nuevo.getFirstname(), "El nombre es obligatorio");
        requerido(nuevo.getLastname(), "Los apellidos son obligatorios");
        validarRolYArea(nuevo.getRole(), nuevo.getAreaSaludId());

        if (usuarios.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        return usuarios.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(nuevo.getPassword()))
                .firstname(nuevo.getFirstname().trim())
                .lastname(nuevo.getLastname().trim())
                .country(nuevo.getCountry())
                .role(nuevo.getRole())
                .areaSaludId(nuevo.getAreaSaludId())
                .activo(true)
                .build());
    }

    @Override
    public List<User> listar() {
        return usuarios.findAll();
    }

    @Override
    public User actualizar(Integer id, Role role, Long areaSaludId, boolean activo) {

        User usuario = obtener(id);
        validarRolYArea(role, areaSaludId);

        boolean esUsuarioActual = usuarioActual.actual().map(u -> Objects.equals(u.id(), id)).orElse(false);
        if (esUsuarioActual && !activo) {
            throw new IllegalArgumentException("No puede desactivar su propio usuario");
        }

        boolean dejaDeSerAdminActivo = usuario.getRole() == Role.ADMIN && usuario.isActivo()
                && (role != Role.ADMIN || !activo);
        if (dejaDeSerAdminActivo && usuarios.countActivosByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo");
        }

        usuario.setRole(role);
        usuario.setAreaSaludId(areaSaludId);
        usuario.setActivo(activo);
        return usuarios.save(usuario);
    }

    @Override
    public void restablecerPassword(Integer id, String nuevaPassword) {
        User usuario = obtener(id);
        validarPassword(nuevaPassword);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarios.save(usuario);
    }

    @Override
    public boolean crearAdminInicialSiNoExiste(String username, String password) {

        if (esBlanco(username) || esBlanco(password) || usuarios.countActivosByRole(Role.ADMIN) > 0) {
            return false;
        }
        if (usuarios.findByUsername(username.trim()).isPresent()) {
            return false;
        }

        crear(User.builder()
                .username(username)
                .password(password)
                .firstname("Administrador")
                .lastname("del sistema")
                .role(Role.ADMIN)
                .build());
        return true;
    }

    private User obtener(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio");
        }
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
    }

    /** El médico necesita un área existente y activa; los demás roles pueden no tenerla. */
    private void validarRolYArea(Role role, Long areaSaludId) {

        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (role == Role.MEDICO && areaSaludId == null) {
            throw new IllegalArgumentException("El médico debe tener un área de salud asignada");
        }
        if (areaSaludId != null) {
            AreaSalud area = areas.findById(areaSaludId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe el área de salud con ID: " + areaSaludId));
            if (!area.isActivo()) {
                throw new IllegalArgumentException("El área de salud " + area.getCodigo() + " está desactivada");
            }
        }
    }

    private static String validarUsername(String username) {
        if (esBlanco(username) || !username.trim().matches(PATRON_USERNAME)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo");
        }
        return username.trim();
    }

    private static void validarPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
    }

    private static void requerido(String valor, String mensaje) {
        if (esBlanco(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private static boolean esBlanco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
