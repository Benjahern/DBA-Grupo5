package com.example.Backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Long id;

    private String name;

    @JsonIgnore
    @Column(name = "sector_geometry", columnDefinition = "geometry(Polygon, 4326)")
    private Polygon sectorGeometry;

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY)
    private List<TaskEntity> tasks = new ArrayList<>();
}
