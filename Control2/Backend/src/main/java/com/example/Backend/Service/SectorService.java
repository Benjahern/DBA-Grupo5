package com.example.Backend.Service;

import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    SectorRepository sectorRepository;

    public SectorEntity create(SectorEntity sector) {
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