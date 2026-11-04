
package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.in.UpdateTaskUseCase;
import com.oropouche.tasks.domain.ports.out.TaskRepositotyPort;

import java.util.Optional;

public class UpdateTaskUseCaseImpl implements UpdateTaskUseCase {

    private final TaskRepositotyPort taskRepositotyPort;

    public UpdateTaskUseCaseImpl(TaskRepositotyPort taskRepositotyPort) {
        this.taskRepositotyPort = taskRepositotyPort;
    }

    @Override
    public Optional<Task> UpdateTasks(Long id, Task updateTask) {

        // El ID de la URL es la referencia principal.
        updateTask.setId(id);

        return taskRepositotyPort.update(updateTask);
    }
}
