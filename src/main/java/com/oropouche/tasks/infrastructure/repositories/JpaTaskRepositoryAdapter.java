package com.oropouche.tasks.infrastructure.repositories;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.out.TaskRepositotyPort;
import com.oropouche.tasks.infrastructure.entities.TaskEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaTaskRepositoryAdapter implements TaskRepositotyPort {

  private final JpaTaskRepository jpaTaskRepository;

    public JpaTaskRepositoryAdapter(JpaTaskRepository jpaTaskRepository) {
        this.jpaTaskRepository = jpaTaskRepository;
    }

    @Override
    public Task save(Task task) {
        TaskEntity taskEntity = TaskEntity.fromDomainModel(task);
        TaskEntity saveTaskEntity = jpaTaskRepository.save(taskEntity);
        return saveTaskEntity.toDomainModel();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return jpaTaskRepository.findById(id).map(TaskEntity::toDomainModel);
    }

    @Override
    public List<Task> findAll() {
        return jpaTaskRepository.findAll().stream()
                .map(TaskEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Task> update(Task task) {
        if (jpaTaskRepository.existsById(task.getId())){
            TaskEntity taskEntity =  TaskEntity.fromDomainModel(task);
            TaskEntity updatetaskEntity = jpaTaskRepository.save(taskEntity);
            return Optional.of(updatetaskEntity.toDomainModel());
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteBytId(Long id) {
        if (jpaTaskRepository.existsById(id)){
            jpaTaskRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
