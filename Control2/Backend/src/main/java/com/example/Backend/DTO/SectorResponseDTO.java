package com.example.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectorResponseDTO {
    private Long id;
    private String name;
    private String wktGeometry;
    private double[] centroid;
}
