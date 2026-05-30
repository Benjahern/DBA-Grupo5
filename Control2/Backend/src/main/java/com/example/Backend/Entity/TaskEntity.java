package com.example.Backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Column(name = "creation_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate creationDate;

    @Column(name = "due_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    SectorEntity sector;

    @Column(name = "status")
    String status; // Los estados son vigente, atrasado, completado y completadoAtrasado
}
