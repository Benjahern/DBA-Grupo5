package com.example.Backend.Repository;

import com.example.Backend.Entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    public List<TaskEntity> findBySector_Id(Long sectorId);

    public List<TaskEntity> findByStatus(String status);

    @Query("SELECT t FROM TaskEntity t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    public List<TaskEntity> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM TaskEntity t WHERE (t.status = :status) AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    public List<TaskEntity> findByStatusAndByKeyword(@Param("status")String status, @Param("keyword") String keyword);

    public List<TaskEntity> findByDueDateBetween(LocalDate start, LocalDate end);

}


