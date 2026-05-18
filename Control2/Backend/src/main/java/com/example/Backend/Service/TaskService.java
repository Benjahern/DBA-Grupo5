package com.example.Backend.Service;

import com.example.Backend.Entity.SectorEntity;
import com.example.Backend.Entity.TaskData;
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
import com.example.Backend.Entity.TaskEntity;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectorRepository sectorRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskEntity create(TaskEntity task) {
        return taskRepository.save(task);
    }

    public TaskEntity update(TaskEntity updatedTask) {
        TaskEntity oldTask = taskRepository.findById(updatedTask.getId()).orElseThrow();
        if ((oldTask.getStatus().equals("completado")) || oldTask.getStatus().equals("completadoAtrasado")) {
            throw new RuntimeException("La tarea yá está completada");
        } else {
            SectorEntity sector = getSector(updatedTask.getSectorID()); //Se actualiza las lista de TaskData de cada sector
            List<TaskData> sectorTaskDataList = sector.getTaskList();
            deleteTaskData(sectorTaskDataList, updatedTask.getId());
            TaskData updatedTaskData = createTaskData(updatedTask);
            sectorTaskDataList.add(updatedTaskData);
            sector.setTaskList(sectorTaskDataList);
            sectorRepository.save(sector);
            return taskRepository.save(updatedTask); // Se guarda la tarea actualizada
        }
    }

    public boolean delete(Long id) throws Exception {
        try {
            taskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<TaskEntity> getAllTask() {
        return taskRepository.findAll();
    }

    public TaskEntity getTask(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public List<TaskEntity> getBySector(Long id) {
        return taskRepository.findBySectorID(id);
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
            update(task);
        }
    }

    private TaskData createTaskData(TaskEntity task) {
        String dueDateSting = task.getDueDate().toString();
        String creationDateString = task.getCreationDate().toString();
        UserEntity user = getUser(task.getUserID());
        SectorEntity sector = getSector(task.getSectorID());
        TaskData data = new TaskData();
        data.setId(task.getId());
        data.setTitle(task.getTitle());
        data.setDescription(task.getDescription());
        data.setUsername(user.getName());
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
}
