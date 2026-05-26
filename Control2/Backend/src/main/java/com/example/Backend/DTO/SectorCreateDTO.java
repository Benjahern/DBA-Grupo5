package com.example.Backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SectorCreateDTO {
    private String name;
    private List<CoordinateDTO> coordinates;
}