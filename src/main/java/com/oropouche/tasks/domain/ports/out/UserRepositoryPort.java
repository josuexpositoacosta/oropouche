package com.oropouche.tasks.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;

public interface UserRepositoryPort {

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    User save(User user);

    List<User> findAll();

    long countActivosByRole(Role role);
}
