package com.oropouche.tasks.infrastructure.entities;

import com.oropouche.tasks.domain.models.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "app_area_salud",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"})
    }
)
public class AppAreaSaludEntity {

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
    private Role role;
}