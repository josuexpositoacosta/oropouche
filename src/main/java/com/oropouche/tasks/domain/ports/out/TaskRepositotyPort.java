package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.*;

import java.util.List;
import java.util.Optional;

public interface TaskRepositotyPort {

    Task save (Task task);
    Optional<Task> findById (Long id);
    List<Task> findAll();
    Optional<Task> update (Task task);
    boolean deleteBytId (Long id);
}
