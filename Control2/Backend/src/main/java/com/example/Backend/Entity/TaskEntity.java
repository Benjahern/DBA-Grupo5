package com.example.Backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String title;
    String description;
    LocalDate creationDate;
    LocalDate dueDate;
    Long userID;
    Long sectorID;
    String status; // Los estados son vigente, atrasado, completado y completadoAtrasado
}
