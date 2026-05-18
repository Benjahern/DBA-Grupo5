package com.example.Backend.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskData {
    private Long id;
    private String title;
    private String description;
    private String username;
    private String sector;
    private String creationDate;
    private String dueDate;
    private String status;
}
