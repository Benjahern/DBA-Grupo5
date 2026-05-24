package com.example.Backend.Controller;

import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Service.TaskService;
import com.example.Backend.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService; // Agregamos el servicio de usuarios

    // Actualizamos el constructor para inyectar ambos servicios
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
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
        // 1. Extraemos el email/username del usuario que inició sesión desde el Token JWT
        String userEmail = authentication.getName();

        // 2. Buscamos el registro correspondiente en la base de datos
        UserEntity currentUser = userService.getUserByEmail(userEmail);

        // 3. Forzamos que la tarea pertenezca al usuario autenticado de forma segura
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

}