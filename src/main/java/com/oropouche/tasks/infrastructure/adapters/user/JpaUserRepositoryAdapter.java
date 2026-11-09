package com.oropouche.tasks.infrastructure.adapters.user;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;
import com.oropouche.tasks.infrastructure.entities.UserEntity;
import com.oropouche.tasks.infrastructure.repositories.user.JpaUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findById(Integer id) {
        return jpaUserRepository.findById(id)
                .map(UserEntity::toDomainModel);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username)
                .map(UserEntity::toDomainModel);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomainModel(user);

        UserEntity savedEntity = jpaUserRepository.save(entity);

        return savedEntity.toDomainModel();
    }
}