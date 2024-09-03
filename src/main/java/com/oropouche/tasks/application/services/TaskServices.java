package com.oropouche.tasks.application.services;

import com.oropouche.tasks.domain.models.*;
import com.oropouche.tasks.domain.ports.in.*;

import java.util.List;
import java.util.Optional;

public class TaskServices implements CreateTaskUseCase, RetrieveTaskUseCase,  UpdateTaskUseCase, DeleteTaskUseCase, GetAddicionalTaskInfoUseCase {

    private final CreateTaskUseCase createTaskUseCase;
    private final RetrieveTaskUseCase retrieveTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase;


    public TaskServices(CreateTaskUseCase createTaskUseCase, RetrieveTaskUseCase retrieveTaskUseCase, UpdateTaskUseCase updateTaskUseCase, DeleteTaskUseCase deleteTaskUseCase, GetAddicionalTaskInfoUseCase getAddicionalTaskInfoUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.retrieveTaskUseCase = retrieveTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.getAddicionalTaskInfoUseCase = getAddicionalTaskInfoUseCase;
    }

    @Override
    public Task CreateTasks(Task tasks) {
        return createTaskUseCase.CreateTasks(tasks);
    }

    @Override
    public AddicionalTaskInfo getAddicionalTaskInfo(Long id) {
        return getAddicionalTaskInfoUseCase.getAddicionalTaskInfo(id);
    }


    @Override
    public Optional<Task> getTask(Long id) {
        return retrieveTaskUseCase.getTask(id);
    }

    @Override
    public List<Task> getAllTask() {
        return retrieveTaskUseCase.getAllTask();
    }

    @Override
    public Optional<Task> UpdateTasks(Long id, Task UpdateTask) {
        return updateTaskUseCase.UpdateTasks(id, UpdateTask);
    }

    @Override
    public boolean deleteTask(Long id) {
        return deleteTaskUseCase.deleteTask(id);
    }

}
