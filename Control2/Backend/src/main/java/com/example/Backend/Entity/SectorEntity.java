package com.example.Backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "sectors")
@AllArgsConstructor
@NoArgsConstructor
public class SectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon geoLocation;

    @ElementCollection
    @CollectionTable(name = "sector_tasks", joinColumns = @JoinColumn(name = "sector_id"))
    List<TaskData> taskList = new ArrayList<>();
}
