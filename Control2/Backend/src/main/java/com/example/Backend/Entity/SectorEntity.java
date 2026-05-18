package com.example.Backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
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

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geoLocation;

    @ElementCollection
    @CollectionTable(name = "sector_tasks", joinColumns = @JoinColumn(name = "task_id"))
    List<TaskData> taskList = new java.util.ArrayList<>();
}
