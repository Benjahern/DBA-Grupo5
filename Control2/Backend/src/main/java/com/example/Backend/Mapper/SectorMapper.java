package com.example.Backend.Mapper;

import com.example.Backend.DTO.SectorMapDTO;
import com.example.Backend.Entity.SectorEntity;
import org.springframework.stereotype.Component;

@Component
public class SectorMapper {

    public SectorMapDTO toMapDTO(SectorEntity sector) {

        SectorMapDTO dto = new SectorMapDTO();

        dto.setId(sector.getId());
        dto.setName(sector.getName());

        if (sector.getSectorGeometry() != null) {
            dto.setWktGeometry(
                    sector.getSectorGeometry().toText()
            );
        }

        return dto;
    }
}
