package com.example.Backend.Controller;

import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Service.TaskService;
import com.example.Backend.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.Backend.Repository.Projection.SectorCountProjection;

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
    public ResponseEntity<List<TaskEntity>> getAllTask(){
        List<TaskEntity> taskList = taskService.getAllTask();
        return ResponseEntity.ok(taskList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTask(@PathVariable Long id){
        TaskEntity task = taskService.getTask(id);
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
    public ResponseEntity<TaskEntity> updateTask(@RequestBody TaskEntity task){
        TaskEntity updatedTask = taskService.update(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTask(@PathVariable Long id) throws Exception {
        Boolean deleted = taskService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    @RequestMapping("/sector/{sectorId}")
    public ResponseEntity<List<TaskEntity>> getBySector(@PathVariable Long sectorId){
        List<TaskEntity> taskList = taskService.getBySector(sectorId);
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/status")
    public ResponseEntity<List<TaskEntity>> getByStatus(@RequestParam(required = false) String status){
        List<TaskEntity> taskList = taskService.getByStatus(status);
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/keyword")
    public ResponseEntity<List<TaskEntity>> getByKeyword(@RequestParam(required = false) String keyword){
        List<TaskEntity> taskList = taskService.getByKeyword(keyword);
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping("/statusAndKeyword")
    public ResponseEntity<List<TaskEntity>> getByStatusAndKeyword(@RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String keyword){
        List<TaskEntity> taskList = taskService.getByStatusAndKeyword(status, keyword);
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

    @GetMapping("/my/top-sector-2km")
    public ResponseEntity<SectorCountProjection> getTopSectorWithin2Km(Authentication authentication) {
        // Obtenemos el usuario autenticado
        UserEntity user = userRepository.findByUserName(authentication.getName());
        
        SectorCountProjection topSector = taskService.getTopSectorCompletedWithin2Km(user.getId());
        
        // Si no hay tareas completadas en ese radio, la consulta nativa devuelve nulo.
        if (topSector == null) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        }
        
        return ResponseEntity.ok(topSector);
    }

    @GetMapping("/my/top-sector-5km")
    public ResponseEntity<SectorCountProjection> getTopSectorWithin5Km(Authentication authentication) {
        // Obtenemos el usuario autenticado
        UserEntity user = userRepository.findByUserName(authentication.getName());
        
        SectorCountProjection topSector = taskService.getTopSectorCompletedWithin5Km(user.getId());
        
        // Si no hay tareas completadas en ese radio
        if (topSector == null) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        }
        
        return ResponseEntity.ok(topSector);
    }

}