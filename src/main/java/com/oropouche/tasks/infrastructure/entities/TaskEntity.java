package com.oropouche.tasks.infrastructure.entities;


import com.oropouche.tasks.domain.models.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private boolean completed;

    

    public TaskEntity(Long id, String title, String description, LocalDateTime creationDate, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.completed = completed;
    }

    public static TaskEntity fromDomainModel(Task task){
     return new TaskEntity(task.getId(),task.getTitle(),task.getDescription(), task.getCreationDate(), task.isCompleted());
    }

    public Task toDomainModel(){
      return new Task(id,title,description,creationDate,completed);
    }

}
