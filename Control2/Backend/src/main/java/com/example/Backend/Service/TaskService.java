package com.example.Backend.Service;

import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Entity.TaskData;
import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Repository.SectorRepository;
import com.example.Backend.Repository.TaskRepository;
import com.example.Backend.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectorRepository sectorRepository;

    @Autowired
    NotificationService notificationService;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskEntity create(TaskEntity task) {
        if (task.getCreationDate() == null) {
            task.setCreationDate(LocalDate.now());
        }
        if (task.getDueDate() == null) {
            throw new RuntimeException("La fecha de vencimiento es obligatoria");
        }
        if (task.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de vencimiento no puede ser anterior a hoy");
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("vigente");
        }
        TaskEntity savedTask = taskRepository.save(task);

        // Crear notificación si la tarea vence mañana
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (savedTask.getDueDate().equals(tomorrow) && notificationService != null) {
            notificationService.createExpiringNotification(savedTask);
        }

        return savedTask;
    }

    public TaskEntity update(TaskEntity updatedTask) {
        TaskEntity oldTask = taskRepository.findById(updatedTask.getId()).orElseThrow();
        if ((oldTask.getStatus().equals("completado")) || oldTask.getStatus().equals("completadoAtrasado")) {
            throw new RuntimeException("La tarea yá está completada");
        } else {
            SectorEntity sector = getSector(updatedTask.getSector().getId()); //Se actualiza las lista de TaskData de cada sector
            List<TaskData> sectorTaskDataList = sector.getTaskList();
            deleteTaskData(sectorTaskDataList, updatedTask.getId());
            TaskData updatedTaskData = createTaskData(updatedTask);
            sectorTaskDataList.add(updatedTaskData);
            sector.setTaskList(sectorTaskDataList);
            sectorRepository.save(sector);
            return taskRepository.save(updatedTask); // Se guarda la tarea actualizada
        }
    }

    @Transactional
    public boolean delete(Long id) {
        TaskEntity task = taskRepository.findById(id).orElseThrow();

        if (task.getSector() != null) {
            SectorEntity sector = getSector(task.getSector().getId());
            List<TaskData> sectorTaskDataList = sector.getTaskList();
            deleteTaskData(sectorTaskDataList, id);
            sector.setTaskList(sectorTaskDataList);
            sectorRepository.save(sector);
        }

        if (notificationService != null) {
            notificationService.deleteByTaskId(id);
        }

        taskRepository.delete(task);
        return true;
    }

    public List<TaskEntity> getAllTask() {
        return taskRepository.findAll();
    }

    public TaskEntity getTask(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public List<TaskEntity> getBySector(Long id) {
        return taskRepository.findBySector_Id(id);
    }

    public List<TaskEntity> getByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    public List<TaskEntity> getByKeyword(String keyword) {
        return taskRepository.findByKeyword(keyword);
    }

    public List<TaskEntity> getByStatusAndKeyword(String status, String keyword) {
        return taskRepository.findByStatusAndByKeyword(status, keyword);
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllTasks() {
        LocalDate now = LocalDate.now();
        List<TaskEntity> allTasks = taskRepository.findByStatus("vigente");
        for (TaskEntity task : allTasks) {
            LocalDate limit = task.getDueDate();
            if (now.isAfter(limit)) {
                task.setStatus("atrasado");
            }
            if (limit.equals(now.plusDays(1)) && !notificationService.notificationExistsForTask(task.getId(), "expiring")) {
                notificationService.createExpiringNotification(task);
            }
            update(task);
        }
    }

    private TaskData createTaskData(TaskEntity task) {
        String dueDateSting = task.getDueDate().toString();
        String creationDateString = task.getCreationDate().toString();
        UserEntity user = getUser(task.getUser().getId());
        SectorEntity sector = getSector(task.getSector().getId());
        TaskData data = new TaskData();
        data.setId(task.getId());
        data.setTitle(task.getTitle());
        data.setDescription(task.getDescription());
        data.setUsername(user.getUserName());
        data.setSector(sector.getName());
        data.setCreationDate(creationDateString);
        data.setDueDate(dueDateSting);
        data.setStatus(task.getStatus());
        return data;
    }

    private void deleteTaskData(List<TaskData> tasks, Long id) {
        Iterator<TaskData> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            TaskData taskData = iterator.next();
            if (taskData.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    private UserEntity getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }
    private SectorEntity getSector(Long id) {
        return sectorRepository.findById(id).orElseThrow();
    }


    public List<TaskEntity> getTasksExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return taskRepository.findByDueDateBetween(today, tomorrow)
                .stream()
                .filter(t -> !t.getStatus().equals("completado")
                        && !t.getStatus().equals("completadoAtrasado"))
                .toList();
    }

    public Map<Long, Long> getTasksCountBySectorForUser(Long userId) {
        List<TaskEntity> tasks = taskRepository.findByUserId(userId);
        Map<Long, Long> sectorCount = new java.util.HashMap<>();
        for (TaskEntity task : tasks) {
            if (task.getSector() != null) {
                Long sectorId = task.getSector().getId();
                sectorCount.merge(sectorId, 1L, Long::sum);
            }
        }
        return sectorCount;
    }

    public TaskEntity getNearestPendingTask(Double userLat, Double userLon, Long userId) {
        List<TaskEntity> pendingTasks = taskRepository.findByUserIdAndStatus(userId, "vigente");

        TaskEntity nearestTask = null;
        double minDistance = Double.MAX_VALUE;

        for (TaskEntity task : pendingTasks) {
            if (task.getSector() != null && task.getSector().getGeoLocation() != null) {
                double taskLat = task.getSector().getGeoLocation().getY();
                double taskLon = task.getSector().getGeoLocation().getX();

                double distance = calculateDistance(userLat, userLon, taskLat, taskLon);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestTask = task;
                }
            }
        }
        return nearestTask;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public SectorCountProjection getTopSectorCompletedWithin2Km(Long userId) {
        return taskRepository.findTopSectorCompletedWithin2Km(userId);
    }

    public SectorCountProjection getTopSectorCompletedWithin5Km(Long userId) {
        return taskRepository.findTopSectorCompletedWithin5Km(userId);
    }

}
