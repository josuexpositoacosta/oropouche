
package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    User save(User user);
}
