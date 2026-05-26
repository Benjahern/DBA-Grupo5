package com.example.Backend.Service;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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

        GeometryFactory geometryFactory = new GeometryFactory();

        SectorEntity sector = new SectorEntity();
        sector.setName(dto.getName());
        sector.setGeoLocation(geometryFactory.createPoint(new Coordinate(coordsDTO.get(0).getLongitude(), coordsDTO.get(0).getLatitude())));

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