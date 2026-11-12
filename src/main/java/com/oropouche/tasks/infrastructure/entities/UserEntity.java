
package com.oropouche.tasks.infrastructure.entities;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"})
    }
)
public class UserEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Basic
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String lastname;

    private String firstname;

    private String country;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Column(name = "area_salud_id")
    private Long areaSaludId;

    @Column(nullable = false)
    private boolean activo;

    public User toDomainModel() {
        return User.builder()
                .id(id)
                .username(username)
                .lastname(lastname)
                .firstname(firstname)
                .country(country)
                .password(password)
                .role(role)
                .areaSaludId(areaSaludId)
                .activo(activo)
                .build();
    }

    public static UserEntity fromDomainModel(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .lastname(user.getLastname())
                .firstname(user.getFirstname())
                .country(user.getCountry())
                .password(user.getPassword())
                .role(user.getRole())
                .areaSaludId(user.getAreaSaludId())
                .activo(user.isActivo())
                .build();
    }
}
