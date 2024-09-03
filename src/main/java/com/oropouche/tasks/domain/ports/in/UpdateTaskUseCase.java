package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.*;

import java.util.Optional;

public interface UpdateTaskUseCase {

   Optional<Task> UpdateTasks (Long id, Task UpdateTask);
}
