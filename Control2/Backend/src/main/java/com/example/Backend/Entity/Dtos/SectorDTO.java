package com.example.Backend.Entity.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@AllArgsConstructor
public class SectorDTO {
    Long id;
    String name;
    double[] coordinates; // [longitude, latitude]

    public static SectorDTO fromEntity(Point geoLocation, Long id, String name) {
        double[] coords = null;
        if (geoLocation != null) {
            coords = new double[]{geoLocation.getX(), geoLocation.getY()};
        }
        return new SectorDTO(id, name, coords);
    }
}