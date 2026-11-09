
package com.oropouche.tasks.application.services.user;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.models.user.UserDTO;
import com.oropouche.tasks.domain.models.user.UserRequest;
import com.oropouche.tasks.domain.models.user.UserResponse;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public UserResponse updateUser(UserRequest userRequest) {

        userRepositoryPort.findById(userRequest.getId())
                .ifPresent(user -> {
                    user.setFirstname(userRequest.getFirstname());
                    user.setLastname(userRequest.getLastname());
                    user.setCountry(userRequest.getCountry());

                    userRepositoryPort.save(user);
                });

        return new UserResponse(
                "El usuario se registró satisfactoriamente"
        );
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Integer id) {

        return userRepositoryPort.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    private UserDTO toDTO(User user) {

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .country(user.getCountry())
                .build();
    }
}
