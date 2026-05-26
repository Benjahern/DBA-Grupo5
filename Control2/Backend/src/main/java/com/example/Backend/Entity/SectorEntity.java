package com.example.Backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

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
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geoLocation;

    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "sector_tasks", joinColumns = @JoinColumn(name = "sector_id"))
    List<TaskData> taskList = new ArrayList<>();

    public double[] getCoordinates() {
        if (geoLocation != null) {
            return new double[]{geoLocation.getX(), geoLocation.getY()};
        }
        return null;
    }
}
