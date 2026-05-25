package com.example.Backend.Service;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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
        // Lógica de validación y cierre de polígono encapsulada aquí
        List<CoordinateDTO> coordsDTO = dto.getCoordinates();
        CoordinateDTO first = coordsDTO.getFirst();
        CoordinateDTO last = coordsDTO.getLast();

        if (Double.compare(first.getLatitude(), last.getLatitude()) != 0 ||
                Double.compare(first.getLongitude(), last.getLongitude()) != 0) {
            coordsDTO.add(first);
        }

        // Conversión a JTS
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] coords = new Coordinate[coordsDTO.size()];
        for (int i = 0; i < coordsDTO.size(); i++) {
            coords[i] = new Coordinate(coordsDTO.get(i).getLongitude(), coordsDTO.get(i).getLatitude());
        }

        SectorEntity sector = new SectorEntity();
        sector.setName(dto.getName());
        sector.setGeoLocation(geometryFactory.createPolygon(coords));

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