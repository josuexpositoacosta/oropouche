package com.oropouche.tasks.domain.models;

import lombok.*;


import java.time.LocalDateTime;


@NoArgsConstructor
@Setter
@Getter
public class Task {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private boolean completed;

    public Task(Long id, String title, String description, LocalDateTime creationDate, boolean completed) {


        this.id = id;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.completed = completed;

    }

}
