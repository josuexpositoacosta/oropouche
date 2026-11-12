package com.oropouche.tasks.infrastructure.adapters.user;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.infrastructure.entities.UserEntity;
import com.oropouche.tasks.infrastructure.repositories.user.JpaUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaUserRepositoryAdapter.class)
class JpaUserRepositoryAdapterTest {

    @Autowired
    private JpaUserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @BeforeEach
    void setUp() {
        jpaUserRepository.deleteAll();
    }

    @Test
    void shouldSaveUserAndGenerateId() {

        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .firstname("Test")
                .lastname("User")
                .country("Cuba")
                .role(Role.MEDICO)
                .build();

        User savedUser = userRepositoryAdapter.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("Test", savedUser.getFirstname());
        assertEquals("User", savedUser.getLastname());
        assertEquals("Cuba", savedUser.getCountry());
        assertEquals(Role.MEDICO, savedUser.getRole());

        assertTrue(jpaUserRepository.existsById(savedUser.getId()));
    }

    @Test
    void shouldFindUserById() {

        UserEntity entity = UserEntity.builder()
                .username("findbyid")
                .password("encodedPassword")
                .firstname("Find")
                .lastname("ById")
                .country("Cuba")
                .role(Role.MEDICO)
                .build();

        UserEntity savedEntity = jpaUserRepository.save(entity);

        Optional<User> result =
                userRepositoryAdapter.findById(savedEntity.getId());

        assertTrue(result.isPresent());

        User user = result.orElseThrow();

        assertEquals(savedEntity.getId(), user.getId());
        assertEquals("findbyid", user.getUsername());
        assertEquals("Find", user.getFirstname());
        assertEquals("ById", user.getLastname());
    }

    @Test
    void shouldFindUserByUsername() {

        UserEntity entity = UserEntity.builder()
                .username("findbyusername")
                .password("encodedPassword")
                .firstname("Find")
                .lastname("ByUsername")
                .country("Cuba")
                .role(Role.MEDICO)
                .build();

        jpaUserRepository.save(entity);

        Optional<User> result =
                userRepositoryAdapter.findByUsername("findbyusername");

        assertTrue(result.isPresent());

        User user = result.orElseThrow();

        assertEquals("findbyusername", user.getUsername());
        assertEquals("Find", user.getFirstname());
        assertEquals("ByUsername", user.getLastname());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {

        Optional<User> result =
                userRepositoryAdapter.findById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {

        Optional<User> result =
                userRepositoryAdapter.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }
}