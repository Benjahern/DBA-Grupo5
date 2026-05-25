package com.example.Backend.Controller;

import com.example.Backend.DTO.SectorCreateDTO;
import com.example.Backend.DTO.SectorResponseDTO;
import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Repository.SectorRepository;
import com.example.Backend.Service.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sectors")
public class SectorController {

    @Autowired
    private SectorService sectorService;
    @Autowired
    private SectorRepository sectorRepository;

    @GetMapping
    public ResponseEntity<List<SectorResponseDTO>> getAllSectors() {
        List<SectorEntity> entities = sectorRepository.findAll();

        return ResponseEntity.ok(entities.stream()
                .map(entity -> new SectorResponseDTO(
                        entity.getId(),
                        entity.getName(),
                        entity.getGeoLocation().toText() // <--- CORREGIDO AQUÍ
                ))
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<SectorResponseDTO> createSector(@RequestBody SectorCreateDTO sectorDTO) {
        // 1. Validación básica de contrato
        if (sectorDTO.getCoordinates() == null || sectorDTO.getCoordinates().size() < 3) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Llamada al servicio
        SectorEntity savedSector = sectorService.createSectorFromDTO(sectorDTO);

        // 3. Transformación a DTO de respuesta
        SectorResponseDTO response = new SectorResponseDTO(
                savedSector.getId(),
                savedSector.getName(),
                savedSector.getGeoLocation().toText()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorEntity> getSector(@PathVariable Long id) {
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }
}