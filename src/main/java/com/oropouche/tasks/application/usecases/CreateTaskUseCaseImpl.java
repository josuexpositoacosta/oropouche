package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.in.*;
import com.oropouche.tasks.domain.ports.out.*;

public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepositotyPort taskRepositotyPort;

    public CreateTaskUseCaseImpl(TaskRepositotyPort taskRepositotyPort) {
        this.taskRepositotyPort = taskRepositotyPort;
    }

    @Override
    public Task CreateTasks(Task tasks) {
        return taskRepositotyPort.save(tasks);
    }
}
