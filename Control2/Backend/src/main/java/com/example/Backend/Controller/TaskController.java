package com.example.Backend.Controller;

import com.example.Backend.Entity.TaskEntity;
import com.example.Backend.Service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
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
    public ResponseEntity<TaskEntity> createTask(@RequestBody TaskEntity task){
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
}
