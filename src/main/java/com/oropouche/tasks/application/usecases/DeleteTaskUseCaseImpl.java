package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.ports.in.*;
import com.oropouche.tasks.domain.ports.out.*;

public class DeleteTaskUseCaseImpl implements DeleteTaskUseCase {

   private final TaskRepositotyPort taskRepositotyPort;

    public DeleteTaskUseCaseImpl(TaskRepositotyPort taskRepositotyPort) {
        this.taskRepositotyPort = taskRepositotyPort;
    }

    @Override
    public boolean deleteTask(Long id) {
        return taskRepositotyPort.deleteBytId(id);
    }




}
