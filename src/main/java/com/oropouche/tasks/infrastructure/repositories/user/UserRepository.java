package com.oropouche.tasks.infrastructure.repositories.user;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUsername(String username); 
    
    @Modifying()
    @Query("update User u set u.firstname=:firstname, u.lastname=:lastname, u.country=:country where u.id = :id")
    void updateUser(@Param(value = "id") Integer id,   @Param(value = "firstname") String firstname, @Param(value = "lastname") String lastname , @Param(value = "country") String country);

}
