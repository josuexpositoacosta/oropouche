
package com.oropouche.tasks.infrastructure.repositories;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.infrastructure.entities.TaskEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTaskRepositoryAdapter.class)
class JpaTaskRepositoryAdapterTest {

    @Autowired
    private JpaTaskRepositoryAdapter taskRepositoryAdapter;

    @Autowired
    private JpaTaskRepository jpaTaskRepository;

    private LocalDateTime originalCreationDate;

    @BeforeEach
    void setUp() {
        jpaTaskRepository.deleteAll();

        originalCreationDate =
                LocalDateTime.of(2026, 1, 15, 10, 30);
    }

    @Test
    void shouldUpdateExistingTaskAndPreserveCreationDate() {

        TaskEntity entity = new TaskEntity(
                null,
                "Original title",
                "Original description",
                originalCreationDate,
                false
        );

        TaskEntity savedEntity = jpaTaskRepository.save(entity);

        Task updatedTask = new Task(
                savedEntity.getId(),
                "Updated title",
                "Updated description",
                LocalDateTime.now(),
                true
        );

        Optional<Task> result =
                taskRepositoryAdapter.update(updatedTask);

        assertTrue(result.isPresent());

        Task actualTask = result.orElseThrow();

        assertEquals("Updated title", actualTask.getTitle());
        assertEquals("Updated description", actualTask.getDescription());
        assertTrue(actualTask.isCompleted());

        assertEquals(
                originalCreationDate,
                actualTask.getCreationDate()
        );

        assertEquals(savedEntity.getId(), actualTask.getId());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() {

        Task task = new Task(
                999L,
                "Nonexistent task",
                "This task does not exist",
                originalCreationDate,
                false
        );

        Optional<Task> result =
                taskRepositoryAdapter.update(task);

        assertTrue(result.isEmpty());

        assertFalse(jpaTaskRepository.existsById(999L));
    }

    @Test
    void shouldDeleteExistingTask() {

        TaskEntity entity = new TaskEntity(
                null,
                "Task to delete",
                "This task should be deleted",
                originalCreationDate,
                false
        );

        TaskEntity savedEntity = jpaTaskRepository.save(entity);

        boolean result =
                taskRepositoryAdapter.deleteBytId(savedEntity.getId());

        assertTrue(result);
        assertFalse(jpaTaskRepository.existsById(savedEntity.getId()));
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentTask() {

        boolean result =
                taskRepositoryAdapter.deleteBytId(999L);

        assertFalse(result);
        assertFalse(jpaTaskRepository.existsById(999L));
    }


}
