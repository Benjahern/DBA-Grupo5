package com.example.Backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sectors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    @JsonIgnore
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon geoLocation;

    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "sector_tasks", joinColumns = @JoinColumn(name = "sector_id"))
    List<TaskData> taskList = new ArrayList<>();

    public double[][] getCoordinates() {
        if (geoLocation != null) {
            Coordinate[] coords = geoLocation.getExteriorRing().getCoordinates();
            double[][] result = new double[coords.length][2];
            for (int i = 0; i < coords.length; i++) {
                result[i] = new double[]{coords[i].getX(), coords[i].getY()};
            }
            return result;
        }
        return null;
    }

    public double[] getCentroid() {
        if (geoLocation != null) {
            Point centroid = geoLocation.getCentroid();
            return new double[]{centroid.getX(), centroid.getY()};
        }
        return null;
    }
}
