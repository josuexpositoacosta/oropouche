package com.oropouche.tasks.infrastructure.repositories;


import com.oropouche.tasks.infrastructure.entities.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


@Repository
public interface JpaTaskRepository extends JpaRepository<TaskEntity,Long> {

}
