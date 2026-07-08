package com.example.Backend.Service;

import com.example.Backend.DTO.NotificationDTO;
import com.example.Backend.Entity.NotificationEntity;
import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Mapper.NotificationMapper;
import com.example.Backend.Repository.NotificationRepository;
import com.example.Backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    public NotificationEntity createNotification(String title, String message, String type, UserEntity user, TaskEntity task) {
        NotificationEntity notification = new NotificationEntity();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(user);
        notification.setTask(task);
        return notificationRepository.save(notification);
    }

    public List<NotificationDTO> getByUser(Long userId) {
        return notificationMapper.toDTOList(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    public List<NotificationDTO> getUnreadByUser(Long userId) {
        return notificationMapper.toDTOList(
                notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
        );
    }

    private List<NotificationEntity> getUnreadEntitiesByUser(Long userId) {
        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    public NotificationDTO markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        NotificationEntity saved = notificationRepository.save(notification);
        return notificationMapper.toDTO(saved);
    }

    public void markAllAsRead(Long userId) {
        List<NotificationEntity> unreadNotifications = getUnreadEntitiesByUser(userId);
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    public NotificationEntity createExpiringNotification(TaskEntity task) {
        String title = "Tarea por vencer";
        LocalDate today = LocalDate.now();
        if (task.getDueDate().equals(today)) {
            String message = "La tarea \"" + task.getTitle() + "\" vence hoy";
            return createNotification(title, message, "expiring", task.getUser(), task);
        }
        String message = "La tarea \"" + task.getTitle() + "\" vence mañana";
        return createNotification(title, message, "expiring", task.getUser(), task);
    }

    public boolean notificationExistsForTask(Long taskId, String type) {
        return notificationRepository.existsByTaskIdAndType(taskId, type);
    }

    public void deleteByTaskId(Long taskId) {
        notificationRepository.deleteByTaskId(taskId);
    }
}
