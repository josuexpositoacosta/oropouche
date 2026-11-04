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

    return jpaTaskRepository.findById(task.getId())
            .map(existingTask -> {

                // Actualizar únicamente los campos modificables.
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setCompleted(task.isCompleted());

                // Conservar la fecha original de creación.
                TaskEntity updatedTask =
                        jpaTaskRepository.save(existingTask);

                return updatedTask.toDomainModel();
            });
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
