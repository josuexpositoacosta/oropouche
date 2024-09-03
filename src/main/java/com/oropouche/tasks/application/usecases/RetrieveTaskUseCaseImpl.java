package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.in.*;
import com.oropouche.tasks.domain.ports.out.*;

import java.util.List;
import java.util.Optional;

public class RetrieveTaskUseCaseImpl implements RetrieveTaskUseCase {

private final TaskRepositotyPort taskRepositotyPort;


    public RetrieveTaskUseCaseImpl(TaskRepositotyPort taskRepositotyPort) {
        this.taskRepositotyPort = taskRepositotyPort;
    }


    @Override
    public Optional<Task> getTask(Long id) {
        return taskRepositotyPort.findById(id);
    }

    @Override
    public List<Task> getAllTask() {
        return taskRepositotyPort.findAll();
    }
}
