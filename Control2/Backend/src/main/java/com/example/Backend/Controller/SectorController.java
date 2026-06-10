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
                        entity.getSectorGeometry().toText()
                ))
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<?> createSector(@RequestBody SectorCreateDTO sectorDTO) {

        try {
            SectorEntity savedSector = sectorService.createSectorFromDTO(sectorDTO);

            SectorResponseDTO response = new SectorResponseDTO(
                    savedSector.getId(),
                    savedSector.getName(),
                    savedSector.getSectorGeometry().toText()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorEntity> getSector(@PathVariable Long id) {
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }
}