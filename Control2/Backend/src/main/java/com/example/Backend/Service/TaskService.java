package com.example.Backend.Service;

import com.example.Backend.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Backend.Entity.TaskEntity;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskEntity create(TaskEntity task){
        return taskRepository.save(task);
    }

    public TaskEntity update(TaskEntity task){
        TaskEntity oldTask = taskRepository.findById(task.getId()).orElseThrow();
        if (oldTask.getIsDone()){
            throw new RuntimeException("La tarea yá está completada");
        } else {
            return taskRepository.save(task);
        }
    }

    public boolean delete(Long id) throws Exception{
        try{
            taskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<TaskEntity> getAllTask(){
        return taskRepository.findAll();
    }

    public TaskEntity getTask(Long id){
        return taskRepository.findById(id).orElseThrow();
    }

    public List<TaskEntity> getBySector(Long id){
        return taskRepository.findBySectorID(id);
    }

    public List<TaskEntity> getByStatus(Boolean status){
        return taskRepository.findIsDone(status);
    }

    public List<TaskEntity> getByKeyword(String keyword){
        return taskRepository.findByKeyword(keyword);
    }

    public List<TaskEntity> getByStatusAndKeyword(Boolean status, String keyword){
        return taskRepository.findByIsDoneAndByKeyword(status, keyword);
    }
}
