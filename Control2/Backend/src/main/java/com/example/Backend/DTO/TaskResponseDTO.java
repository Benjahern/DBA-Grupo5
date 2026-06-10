package com.example.Backend.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate creationDate;
    private LocalDate dueDate;
    private String status;

    private Long userId;
    private String userName;

    private Long sectorId;
    private String sectorName;

    private CoordinateDTO location;
}
