package com.example.Backend.Service;

import com.example.Backend.DTO.SeedRequest;
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
import com.example.Backend.Repository.Projection.SectorCountProjection;
import com.example.Backend.Repository.Projection.UserSectorCountProjection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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

        // Crear notificación si la tarea vence hoy o mañana
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        if (savedTask.getDueDate().equals(today) || savedTask.getDueDate().equals(tomorrow)) {
            if (notificationService != null) {
                notificationService.createExpiringNotification(savedTask);
            }
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

    public List<TaskEntity> getByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
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
            if ((limit.equals(now) || limit.equals(now.plusDays(1))) && !notificationService.notificationExistsForTask(task.getId(), "expiring")) {
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

    // Métodos para 2km
    public SectorCountProjection getTopSectorCompletedWithin2KmSpecificUser(Long userId) {
        return taskRepository.findTopSectorCompletedWithin2KmSpecificUser(userId);
    }
    public SectorCountProjection getTopSectorCompletedWithin2KmAllUsers(Long userId) {
        return taskRepository.findTopSectorCompletedWithin2KmAllUsers(userId);
    }

    // Métodos para 5km
    public SectorCountProjection getTopSectorCompletedWithin5KmSpecificUser(Long userId) {
        return taskRepository.findTopSectorCompletedWithin5KmSpecificUser(userId);
    }
    public SectorCountProjection getTopSectorCompletedWithin5KmAllUsers(Long userId) {
        return taskRepository.findTopSectorCompletedWithin5KmAllUsers(userId);
    }

    // Promedio de distancia de tareas completadas respecto al usuario
    public Double getAverageDistanceOfCompletedTasks(Long userId) {
        var result = taskRepository.getAverageDistanceOfUserCompletedTasks(userId);
        return result != null ? result.getAverageDistance() : null;
    }

    // Consulta 5: Sectores con más tareas pendientes (filtrado por usuario)
    public List<SectorCountProjection> getSectorsWithMostPendingTasks(Long userId) {
        return taskRepository.findSectorsWithMostPendingTasks(userId);
    }

    // Consulta 6: Tareas por cada usuario por sector (completadas)
    public List<UserSectorCountProjection> getCompletedTasksForEachUserPerSector() {
        return taskRepository.countCompletedTasksForEachUserPerSector();
    }

    // Promedio de distancia entre todas las tareas completadas y el usuario
    public Double getAverageDistanceOfAllCompletedTasks(Long userId) {
        var result = taskRepository.getAverageDistanceOfAllCompletedTasksToUser(userId);
        return result != null ? result.getAverageDistance() : null;
    }

    // Seeder para la creación de múltiples tareas
    @Transactional
    public List<TaskEntity> seedTasks(SeedRequest request) {
        // Validación de existencia de sectores
        long sectorCount = sectorRepository.count();
        if (sectorCount == 0) {
            throw new IllegalStateException("No existen sectores creados. Por favor, crea al menos un sector antes de generar tareas.");
        }

        // Validación de IDs de sector existentes
        if (request.getTargetSectorIds() != null) {
            for (Long sectorId : request.getTargetSectorIds()) {
                if (!sectorRepository.existsById(sectorId)) {
                    throw new IllegalArgumentException("El sector con ID " + sectorId + " no existe.");
                }
            }
        }

        // Validación de seguridad para usuarios
        if (request.getTargetUserIds() == null || request.getTargetSectorIds() == null) {
            throw new IllegalArgumentException("IDs de usuario o sector no pueden ser nulos");
        }

        List<TaskEntity> createdTasks = new ArrayList<>();
        Random rand = new Random();
        LocalDate today = LocalDate.now();

        for (Long userId : request.getTargetUserIds()) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

            for (Long sectorId : request.getTargetSectorIds()) {
                SectorEntity sector = sectorRepository.findById(sectorId)
                        .orElseThrow(() -> new RuntimeException("Sector no encontrado: " + sectorId));

                for (int i = 0; i < request.getCountPerUser(); i++) {
                    TaskEntity task = new TaskEntity();
                    task.setTitle("Tarea " + rand.nextInt(1000));
                    task.setDescription("Generada automáticamente por Seeder");
                    task.setUser(user);
                    task.setSector(sector);

                    String status = getRandomStatus(rand, request.getStatusDistribution());
                    task.setStatus(status);

                    // --- FECHAS ---
                    if (status.toLowerCase().contains("atrasado")) {
                        // Si es atrasado, dueDate es en el pasado
                        int randomDays = rand.nextInt(30) + 1;
                        task.setDueDate(today.minusDays(randomDays));
                        // creationDate debe ser anterior a la fecha de vencimiento
                        task.setCreationDate(task.getDueDate().minusDays(rand.nextInt(10) + 1));
                    } else {
                        // Si es vigente, dueDate es en el futuro
                        task.setCreationDate(today);
                        int randomDays = rand.nextInt(30) + 1;
                        task.setDueDate(today.plusDays(randomDays));
                    }

                    TaskEntity savedTask = taskRepository.save(task);
                    createdTasks.add(savedTask);
                }
            }
        }
        return createdTasks;
    }

    private String getRandomStatus(Random rand, Map<String, Integer> distribution) {
        if (distribution == null || distribution.isEmpty()) return "vigente";

        // Filtrar solo los estados que tienen un peso > 0
        List<Map.Entry<String, Integer>> activeWeights = distribution.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .collect(Collectors.toList());

        if (activeWeights.isEmpty()) return "vigente";

        // Calcular la suma total de los pesos válidos
        int totalWeight = activeWeights.stream().mapToInt(Map.Entry::getValue).sum();

        // Generar un número aleatorio entre 0 y el total
        int r = rand.nextInt(totalWeight);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : activeWeights) {
            cumulative += entry.getValue();
            if (r < cumulative) {
                return entry.getKey();
            }
        }
        return activeWeights.get(activeWeights.size() - 1).getKey();
    }

    @Transactional
    public void deleteAllTasks() {

        List<TaskEntity> allTasks = taskRepository.findAll();

        for (TaskEntity task : allTasks) {
            if (task.getSector() != null) {
                SectorEntity sector = sectorRepository.findById(task.getSector().getId())
                        .orElse(null);
                if (sector != null) {

                    List<TaskData> sectorTaskDataList = sector.getTaskList();
                    deleteTaskData(sectorTaskDataList, task.getId());

                    sector.setTaskList(sectorTaskDataList);
                    sectorRepository.save(sector);
                }
            }

            if (notificationService != null) {
                notificationService.deleteByTaskId(task.getId());
            }
        }

        taskRepository.deleteAll();
    }
}
