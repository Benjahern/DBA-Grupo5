package com.example.Backend.Controller;

import com.example.Backend.DTO.SeedRequest;
import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Service.TaskService;
import com.example.Backend.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.Backend.Repository.Projection.SectorCountProjection;
import com.example.Backend.Repository.Projection.UserSectorCountProjection;
import com.example.Backend.Repository.Projection.ClosestTaskProjection;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<TaskEntity>> getAllTask(Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        List<TaskEntity> taskList;
        if (isAdmin) {
            taskList = taskService.getAllTask();
        } else {
            taskList = taskService.getByUserId(user.getId());
        }
        return ResponseEntity.ok(taskList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTask(@PathVariable Long id, Authentication authentication){
        TaskEntity task = taskService.getTask(id);
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        if (!isAdmin && (task.getUser() == null || !task.getUser().getId().equals(user.getId()))) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskEntity> createTask(@RequestBody TaskEntity task, Authentication authentication){
        String username = authentication.getName();
        UserEntity currentUser = userRepository.findByUserName(username);

        task.setUser(currentUser);

        TaskEntity newTask = taskService.create(task);
        return ResponseEntity.ok(newTask);
    }


    @PutMapping("/update")
    public ResponseEntity<TaskEntity> updateTask(@RequestBody TaskEntity task, Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        TaskEntity existingTask = taskService.getTask(task.getId());
        if (!isAdmin && (existingTask.getUser() == null || !existingTask.getUser().getId().equals(user.getId()))) {
            return ResponseEntity.status(403).build();
        }
        TaskEntity updatedTask = taskService.update(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTask(@PathVariable Long id, Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        TaskEntity task = taskService.getTask(id);
        if (!isAdmin && (task.getUser() == null || !task.getUser().getId().equals(user.getId()))) {
            return ResponseEntity.status(403).build();
        }
        Boolean deleted = taskService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    @RequestMapping("/sector/{sectorId}")
    public ResponseEntity<List<TaskEntity>> getBySector(@PathVariable Long sectorId, Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        List<TaskEntity> taskList;
        if (isAdmin) {
            taskList = taskService.getBySector(sectorId);
        } else {
            taskList = taskService.getBySector(sectorId).stream()
                    .filter(t -> t.getUser() != null && t.getUser().getId().equals(user.getId()))
                    .toList();
        }
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/status")
    public ResponseEntity<List<TaskEntity>> getByStatus(@RequestParam(required = false) String status, Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        List<TaskEntity> taskList;
        if (isAdmin) {
            taskList = taskService.getByStatus(status);
        } else {
            taskList = taskService.getByStatus(status).stream()
                    .filter(t -> t.getUser() != null && t.getUser().getId().equals(user.getId()))
                    .toList();
        }
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/keyword")
    public ResponseEntity<List<TaskEntity>> getByKeyword(@RequestParam(required = false) String keyword, Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        List<TaskEntity> taskList;
        if (isAdmin) {
            taskList = taskService.getByKeyword(keyword);
        } else {
            taskList = taskService.getByKeyword(keyword).stream()
                    .filter(t -> t.getUser() != null && t.getUser().getId().equals(user.getId()))
                    .toList();
        }
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/statusAndKeyword")
    public ResponseEntity<List<TaskEntity>> getByStatusAndKeyword(@RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String keyword,
                                                                  Authentication authentication){
        UserEntity user = userRepository.findByUserName(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        List<TaskEntity> taskList;
        if (isAdmin) {
            taskList = taskService.getByStatusAndKeyword(status, keyword);
        } else {
            taskList = taskService.getByStatusAndKeyword(status, keyword).stream()
                    .filter(t -> t.getUser() != null && t.getUser().getId().equals(user.getId()))
                    .toList();
        }
        return ResponseEntity.ok(taskList);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<TaskEntity>> getExpiringTasks() {
        return ResponseEntity.ok(taskService.getTasksExpiringSoon());
    }

    @GetMapping("/my/sectors-count")
    public ResponseEntity<Map<Long, Long>> getMyTasksCountBySector(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        return ResponseEntity.ok(taskService.getTasksCountBySectorForUser(user.getId()));
    }

    @GetMapping("/my/nearest")
    public ResponseEntity<TaskEntity> getMyNearestTask(
            Authentication authentication,
            @RequestParam Double lat,
            @RequestParam Double lon) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        return ResponseEntity.ok(taskService.getNearestPendingTask(lat, lon, user.getId()));
    }

    @GetMapping("/my/closest-task")
public ResponseEntity<ClosestTaskProjection> getClosestTask(
        Authentication authentication) {

    UserEntity user = userRepository.findByUserName(authentication.getName());

    ClosestTaskProjection task =
            taskService.getClosestPendingTask(user.getId());

    return ResponseEntity.ok(task);
}

    @GetMapping("/my/top-sector-2km")
    public ResponseEntity<SectorCountProjection> getTopSectorWithin2Km(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        
        // Verificamos si el usuario es ADMIN 
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));
        
        SectorCountProjection topSector;
        
        if (isAdmin) {
            topSector = taskService.getTopSectorCompletedWithin2KmAllUsers(user.getId());
        } else {
            topSector = taskService.getTopSectorCompletedWithin2KmSpecificUser(user.getId());
        }
        
        if (topSector == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(topSector);
    }


    @GetMapping("/my/top-sector-5km")
    public ResponseEntity<SectorCountProjection> getTopSectorWithin5Km(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));
        
        SectorCountProjection topSector;
        
        if (isAdmin) {
            topSector = taskService.getTopSectorCompletedWithin5KmAllUsers(user.getId());
        } else {
            topSector = taskService.getTopSectorCompletedWithin5KmSpecificUser(user.getId());
        }
        
        if (topSector == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(topSector);
    }

    //  Promedio de distancia de tareas completadas del usuario
    @GetMapping("/my/average-distance")
    public ResponseEntity<Double> getAverageDistanceOfCompletedTasks(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        return ResponseEntity.ok(taskService.getAverageDistanceOfCompletedTasks(user.getId()));
    }

    // Consulta 5: Sectores con más tareas pendientes (filtrado por usuario)
    @GetMapping("/my/pending/by-sector")
    public ResponseEntity<List<SectorCountProjection>> getMyPendingTasksBySector(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        return ResponseEntity.ok(taskService.getSectorsWithMostPendingTasks(user.getId()));
    }

    //  Tareas por cada usuario por sector (Admin)
    @GetMapping("/all-users/completed-by-sector")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSectorCountProjection>> getAllUsersCompletedBySector() {
        return ResponseEntity.ok(taskService.getCompletedTasksForEachUserPerSector());
    }

    //  Promedio de distancia global (todas las completadas)
    @GetMapping("/my/average-distance-global")
    public ResponseEntity<Double> getAverageDistanceOfAllCompletedTasks(Authentication authentication) {
        UserEntity user = userRepository.findByUserName(authentication.getName());
        return ResponseEntity.ok(taskService.getAverageDistanceOfAllCompletedTasks(user.getId()));
    }

    @PostMapping("/seed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> seedTasks(@RequestBody SeedRequest request) {
        try {
            List<TaskEntity> tasks = taskService.seedTasks(request);
            return ResponseEntity.ok(tasks);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado: " + e.getMessage());
        }
    }

    @DeleteMapping("/seed/clean")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanDatabase() {
        try {
            taskService.deleteAllTasks();
            return ResponseEntity.ok("Base de datos de tareas limpiada exitosamente.");
        } catch (IllegalStateException e) {
            // Captura errores de lógica de negocio
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error crítico en el servidor: " + e.getMessage());
        }
    }
}