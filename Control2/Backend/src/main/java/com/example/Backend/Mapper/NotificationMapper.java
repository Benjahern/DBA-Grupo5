package com.example.Backend.Mapper;

import com.example.Backend.DTO.NotificationDTO;
import com.example.Backend.Entity.NotificationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(NotificationEntity entity) {

        if (entity == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getTask() != null) {
            dto.setTaskId(entity.getTask().getId());
        }

        return dto;
    }

    public List<NotificationDTO> toDTOList(List<NotificationEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}
