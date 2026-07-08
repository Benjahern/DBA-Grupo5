package com.example.Backend.Mapper;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.TaskResponseDTO;
import com.example.Backend.Entity.TaskEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponseDTO(TaskEntity task) {

        TaskResponseDTO dto = new TaskResponseDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setCreationDate(task.getCreationDate());
        dto.setStatus(task.getStatus().name());

        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
            dto.setUserName(task.getUser().getUserName());
        }

        if (task.getSector() != null) {
            dto.setSectorId(task.getSector().getId());
            dto.setSectorName(task.getSector().getName());
        }

        if (task.getTaskLocation() != null) {
            dto.setLocation(toCoordinateDTO(task.getTaskLocation()));
        }

        return dto;
    }

    public CoordinateDTO toCoordinateDTO(Point point) {

        if (point == null) {
            return null;
        }

        return new CoordinateDTO(
                point.getY(),
                point.getX()
        );
    }

    public Point toPoint(CoordinateDTO dto) {

        if (dto == null) {
            return null;
        }

        GeometryFactory geometryFactory = new GeometryFactory(
                new PrecisionModel(),
                4326
        );

        return geometryFactory.createPoint(
                new Coordinate(
                        dto.getLongitude(),
                        dto.getLatitude()
                )
        );
    }
}
