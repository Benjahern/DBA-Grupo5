package com.example.Backend.Controller;

import com.example.Backend.DTO.UserRegisterDTO;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Service.UserService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserRegisterDTO registerDTO) {
        UserEntity user = new UserEntity();
        user.setUserName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());

        // Conversión matemática de Lat/Lng a Point (PostGIS SRID 4326)
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(registerDTO.getLongitude(), registerDTO.getLatitude()));
        user.setGeoLocation(point);

        return ResponseEntity.ok(userService.create(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}