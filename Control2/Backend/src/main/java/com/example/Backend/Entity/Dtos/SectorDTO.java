package com.example.Backend.Entity.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

@Data
@AllArgsConstructor
public class SectorDTO {
    Long id;
    String name;
    double[][] coordinates; // [[longitude, latitude], ...]
    double[] centroid;       // [longitude, latitude]

    public static SectorDTO fromEntity(Polygon geoLocation, Long id, String name) {
        double[][] coords = null;
        double[] centroid = null;
        if (geoLocation != null) {
            Coordinate[] ring = geoLocation.getExteriorRing().getCoordinates();
            coords = new double[ring.length][2];
            for (int i = 0; i < ring.length; i++) {
                coords[i] = new double[]{ring[i].getX(), ring[i].getY()};
            }
            centroid = new double[]{
                    geoLocation.getCentroid().getX(),
                    geoLocation.getCentroid().getY()
            };
        }
        return new SectorDTO(id, name, coords, centroid);
    }
}