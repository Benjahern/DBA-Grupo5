package com.example.Backend.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String status;
    private Long sectorId;
    private CoordinateDTO location;
}
