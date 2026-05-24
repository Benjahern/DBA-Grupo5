package com.example.Backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Point;


@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username")
    String userName;

    @Column(name = "email")
    String email;


    @Column(name= "pass")
    String password;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geoLocation;

    @ElementCollection
    List<Long> taskList = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    private Set<String> roles = new HashSet<>();
}
