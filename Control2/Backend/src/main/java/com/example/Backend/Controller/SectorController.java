package com.example.Backend.Controller;

import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Service.SectorService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sectors")
public class SectorController {

    @Autowired
    private SectorService sectorService;

    @GetMapping
    public ResponseEntity<List<SectorEntity>> getAllSectors() {
        return ResponseEntity.ok(sectorService.getAllSectors());
    }

    @PostMapping
    public ResponseEntity<SectorEntity> createSector(@RequestBody SectorCreateDTO sectorDTO) {
        SectorEntity sector = new SectorEntity();
        sector.setName(sectorDTO.getName());

        // Conversión matemática de Lat/Lng a Point (PostGIS SRID 4326)
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(sectorDTO.getLongitude(), sectorDTO.getLatitude()));
        sector.setGeoLocation(point);

        return ResponseEntity.ok(sectorService.create(sector));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorEntity> getSector(@PathVariable Long id) {
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }

}