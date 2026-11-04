
package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.Task;
import com.oropouche.tasks.domain.ports.out.TaskRepositotyPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTaskUseCaseImplTest {

    @Mock
    private TaskRepositotyPort taskRepositoryPort;

    @InjectMocks
    private UpdateTaskUseCaseImpl updateTaskUseCase;

    @Test
    void shouldUseUrlIdWhenUpdatingTask() {

        Long urlId = 10L;
        Long requestId = 99L;

        Task task = new Task(
                requestId,
                "Updated title",
                "Updated description",
                LocalDateTime.now(),
                true
        );

        when(taskRepositoryPort.update(task))
                .thenReturn(Optional.of(task));

        Optional<Task> result =
                updateTaskUseCase.UpdateTasks(urlId, task);

        assertTrue(result.isPresent());
        assertEquals(urlId, task.getId());

        verify(taskRepositoryPort).update(task);
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() {

        Long id = 100L;

        Task task = new Task(
                id,
                "New title",
                "New description",
                LocalDateTime.now(),
                false
        );

        when(taskRepositoryPort.update(task))
                .thenReturn(Optional.empty());

        Optional<Task> result =
                updateTaskUseCase.UpdateTasks(id, task);

        assertTrue(result.isEmpty());

        verify(taskRepositoryPort).update(task);
    }
}
