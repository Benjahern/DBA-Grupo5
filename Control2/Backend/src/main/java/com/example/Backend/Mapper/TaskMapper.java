package com.example.Backend.Mapper;

import com.example.Backend.DTO.CoordinateDTO;
import com.example.Backend.DTO.TaskResponseDTO;
import com.example.Backend.Entity.TaskEntity;
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
            dto.setLocation(new CoordinateDTO(
                    task.getTaskLocation().getY(),
                    task.getTaskLocation().getX()
            ));
        }

        return dto;
    }
}
