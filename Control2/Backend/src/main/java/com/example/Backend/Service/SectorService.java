package com.example.Backend.Service;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    SectorRepository sectorRepository;

    @Transactional
    public SectorEntity createSectorFromDTO(SectorCreateDTO dto) {
        List<CoordinateDTO> coordsDTO = dto.getCoordinates();

        if (coordsDTO == null || coordsDTO.size() < 3) {
            throw new IllegalArgumentException(
                    "Un sector debe tener al menos 3 coordenadas");
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = new Coordinate[coordsDTO.size() + 1];
        for (int i = 0; i < coordsDTO.size(); i++) {
            coordinates[i] = new Coordinate(
                    coordsDTO.get(i).getLongitude(),
                    coordsDTO.get(i).getLatitude()
            );
        }

        coordinates[coordsDTO.size()] = coordinates[0];
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(ring);

        SectorEntity sector = new SectorEntity();
        sector.setName(dto.getName());
        sector.setSectorGeometry(polygon);

        return sectorRepository.save(sector);
    }

    public List<SectorEntity> getAllSectors() {
        return sectorRepository.findAll();
    }

    public SectorEntity getSectorById(Long id) {
        return sectorRepository.findById(id).orElseThrow(() -> new RuntimeException("Sector no encontrado"));
    }

    public List<SectorEntity> getSectorsNearUser(Point userLocation, double radiusInMetres) {
        return sectorRepository.findSectorsWithinRadius(userLocation, radiusInMetres);
    }
}