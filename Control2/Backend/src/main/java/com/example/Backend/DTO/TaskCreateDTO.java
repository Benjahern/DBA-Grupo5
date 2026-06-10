package com.example.Backend.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskCreateDTO {

    private String title;
    private String description;
    private LocalDate dueDate;
    private Long sectorId;
    private CoordinateDTO location;
}
