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

    // No se si piensar usar la ubicación como Point o como otro tipo de dato
    Point geoLocation;

    @ElementCollection
    List<Long> taskList = new java.util.ArrayList<>();
}
