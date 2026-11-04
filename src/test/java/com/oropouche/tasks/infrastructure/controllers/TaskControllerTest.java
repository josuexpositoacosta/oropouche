package com.oropouche.tasks.infrastructure.controllers;

import com.oropouche.tasks.application.services.TaskServices;
import com.oropouche.tasks.domain.models.AddicionalTaskInfo;
import com.oropouche.tasks.domain.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskServices taskServices;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;

    private Task task;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .build();

        task = new Task(
                1L,
                "Test task",
                "Description",
                LocalDateTime.of(2026, 11, 4, 10, 0),
                false
        );
    }

    @Test
    void shouldCreateTask() throws Exception {
        when(taskServices.CreateTasks(org.mockito.ArgumentMatchers.any(Task.class)))
                .thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Test task",
                                    "description": "Description",
                                    "completed": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"));
    }

    @Test
    void shouldGetTaskById() throws Exception {
        when(taskServices.getTask(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test task"));
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        when(taskServices.getTask(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllTasks() throws Exception {
        when(taskServices.getAllTask()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test task"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        when(taskServices.UpdateTasks(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(Task.class)))
                .thenReturn(Optional.of(task));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated task",
                                    "description": "Updated description",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingTask() throws Exception {
        when(taskServices.UpdateTasks(
                org.mockito.ArgumentMatchers.eq(99L),
                org.mockito.ArgumentMatchers.any(Task.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated task",
                                    "description": "Updated description",
                                    "completed": true
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        when(taskServices.deleteTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingTask() throws Exception {
        when(taskServices.deleteTask(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAdditionalTaskInfo() throws Exception {
        AddicionalTaskInfo info =
                new AddicionalTaskInfo(10L, "John Doe", "john@example.com");

        when(taskServices.getAddicionalTaskInfo(1L)).thenReturn(info);

        mockMvc.perform(get("/api/tasks/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.userEmail").value("john@example.com"));
    }

@Test
void shouldReturnNotFoundWhenAdditionalTaskInfoDoesNotExist() throws Exception {
    when(taskServices.getAddicionalTaskInfo(99L)).thenReturn(null);

    mockMvc.perform(get("/api/tasks/details/99"))
            .andExpect(status().isNotFound());
}

}
