package com.oropouche.tasks.infrastructure.controllers;

import com.oropouche.tasks.application.services.TaskServices;
import com.oropouche.tasks.domain.models.AddicionalTaskInfo;
import com.oropouche.tasks.domain.models.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen
public class TaskController {

      private final TaskServices taskServices;


    public TaskController(TaskServices taskServices) {
        this.taskServices = taskServices;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task){
        Task createdTask = taskServices.CreateTasks(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

@GetMapping("/{taskId}")
    //@GetMapping("/api/tasks/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId){
        return taskServices.getTask(taskId)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTask(){
        List<Task> tasks = taskServices.getAllTask();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }


    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task updateTask){
        return taskServices.UpdateTasks(taskId, updateTask)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId){
        if(taskServices.deleteTask(taskId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


@GetMapping("/details/{taskId}")
public ResponseEntity<AddicionalTaskInfo> getAddicionalTaskInfo(@PathVariable Long taskId) {
    AddicionalTaskInfo info = taskServices.getAddicionalTaskInfo(taskId);

    return info != null
            ? ResponseEntity.ok(info)
            : ResponseEntity.notFound().build();
}

}
