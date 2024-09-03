package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.in.*;
import com.oropouche.tasks.domain.ports.out.*;

import java.util.Optional;

public class UpdateTaskUseCaseImpl implements UpdateTaskUseCase {

    private final TaskRepositotyPort taskRepositotyPort;


    public UpdateTaskUseCaseImpl(TaskRepositotyPort taskRepositotyPort) {
        this.taskRepositotyPort = taskRepositotyPort;
    }

    @Override
    public Optional<Task> UpdateTasks(Long id, Task UpdateTask) {
        return taskRepositotyPort.update(UpdateTask);
    }
}
