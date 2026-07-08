package com.example.Backend.Service;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    SectorRepository sectorRepository;

    @Autowired
    com.example.Backend.Repository.TaskRepository taskRepository;

    @Transactional
    public SectorEntity createSectorFromDTO(SectorCreateDTO dto) {
        List<CoordinateDTO> coordsDTO = dto.getCoordinates();

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Construir el anillo del polígono con todas las coordenadas
        // JTS requiere que el primer y último punto sean iguales (anillo cerrado)
        Coordinate[] polygonCoords = new Coordinate[coordsDTO.size() + 1];
        for (int i = 0; i < coordsDTO.size(); i++) {
            polygonCoords[i] = new Coordinate(
                    coordsDTO.get(i).getLongitude(),
                    coordsDTO.get(i).getLatitude()
            );
        }
        // Cerrar el anillo repitiendo el primer punto
        polygonCoords[coordsDTO.size()] = polygonCoords[0];

        Polygon polygon = geometryFactory.createPolygon(polygonCoords);

        SectorEntity sector = new SectorEntity();
        sector.setName(dto.getName());
        sector.setGeoLocation(polygon);

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

    @Transactional
    public SectorEntity updateSector(Long id, SectorCreateDTO dto) {
        SectorEntity sector = getSectorById(id);
        
        // Solo actualizamos el nombre por ahora según el plan
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            sector.setName(dto.getName());
        }

        // Si se proveen coordenadas, también actualizamos el polígono (opcional, por si el frontend lo envía)
        if (dto.getCoordinates() != null && dto.getCoordinates().size() >= 3) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            List<CoordinateDTO> coordsDTO = dto.getCoordinates();
            Coordinate[] polygonCoords = new Coordinate[coordsDTO.size() + 1];
            for (int i = 0; i < coordsDTO.size(); i++) {
                polygonCoords[i] = new Coordinate(coordsDTO.get(i).getLongitude(), coordsDTO.get(i).getLatitude());
            }
            polygonCoords[coordsDTO.size()] = polygonCoords[0];
            sector.setGeoLocation(geometryFactory.createPolygon(polygonCoords));
        }

        return sectorRepository.save(sector);
    }

    @Transactional
    public void deleteSector(Long id) {
        SectorEntity sector = getSectorById(id);
        
        // Verificar si existen tareas asociadas
        List<com.example.Backend.Entity.TaskEntity> associatedTasks = taskRepository.findBySector_Id(id);
        if (associatedTasks != null && !associatedTasks.isEmpty()) {
            throw new RuntimeException("No se puede eliminar el sector porque tiene " + associatedTasks.size() + " tarea(s) asociada(s).");
        }
        
        sectorRepository.delete(sector);
    }
}