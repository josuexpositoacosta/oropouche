package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.Task;

import java.util.List;
import java.util.Optional;

public interface RetrieveTaskUseCase {
    Optional<Task> getTask (Long id);
    List<Task> getAllTask();
}
