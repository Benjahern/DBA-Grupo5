package com.example.Backend.Repository;

import com.example.Backend.Entity.TaskEntity;

import com.example.Backend.Repository.Projection.DistanceProjection;
import com.example.Backend.Repository.Projection.SectorCountProjection;
import com.example.Backend.Repository.Projection.UserSectorCountProjection;
import com.example.Backend.Repository.Projection.ClosestTaskProjection;

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
    public List<TaskEntity> findByStatusAndByKeyword(@Param("status") String status, @Param("keyword") String keyword);

    public List<TaskEntity> findByDueDateBetween(LocalDate start, LocalDate end);


    List<TaskEntity> findByUserId(Long userId);

    List<TaskEntity> findByUserIdAndStatus(Long userId, String status);

    //consultas de la 1 a la 8

    /**
     * 1. ¿Cuántas tareas ha hecho el usuario por sector?
     */
    @Query(value = "SELECT u.username AS userName, s.id AS sectorId, s.name AS sectorName, COUNT(t.id) AS taskCount " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "JOIN users u ON t.user_id = u.id " +
            "WHERE t.user_id = :userId AND t.status IN ('completado', 'completadoAtrasado') " +
            "GROUP BY u.username, s.id, s.name", nativeQuery = true)
    List<SectorCountProjection> countCompletedTasksBySpecificUserPerSector(@Param("userId") Long userId);

    /**
     * 2. ¿Cuál es la tarea más cercana al usuario (que esté pendiente)?
     */
    @Query(value = "SELECT t.id AS taskId, t.title AS title, t.description AS description, " +
            "t.due_date AS dueDate, t.status AS status, s.name AS sectorName, " +
            "ST_Distance(s.geo_location::geography, u.geo_location::geography) AS distanceMetres " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "JOIN users u ON u.id = :userId " +
            "WHERE t.status IN ('vigente', 'atrasado') " +
            "ORDER BY ST_Distance(s.geo_location::geography, u.geo_location::geography) ASC " +
            "LIMIT 1", nativeQuery = true)
    ClosestTaskProjection findClosestPendingTask(@Param("userId") Long userId);

    /**
     * 3. ¿Cuál es el sector con más tareas completadas en un radio de 2 kilómetros del usuario?
     */
    @Query(value = "SELECT s.id AS sectorId, s.name AS sectorName, COUNT(t.id) AS taskCount " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "JOIN users u ON t.user_id = u.id " + 
            "WHERE u.id = :userId " +             
            "AND t.status IN ('completado', 'completadoAtrasado') " +
            "AND ST_DWithin(s.geo_location::geography, u.geo_location::geography, 2000) " +
            "GROUP BY s.id, s.name " +
            "ORDER BY taskCount DESC " +
            "LIMIT 1", nativeQuery = true)
    SectorCountProjection findTopSectorCompletedWithin2Km(@Param("userId") Long userId);
    /**
     * 4. ¿Cuál es el promedio de distancia de las tareas completadas respecto a la ubicación del usuario?
     */
    @Query(value = "SELECT AVG(ST_Distance(s.geo_location::geography, u.geo_location::geography)) AS averageDistance " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "JOIN users u ON u.id = :userId " +
            "WHERE t.user_id = :userId AND t.status IN ('completado', 'completadoAtrasado')", nativeQuery = true)
    DistanceProjection getAverageDistanceOfUserCompletedTasks(@Param("userId") Long userId);

    /**
     * 5. ¿En qué sectores geográficos se concentran la mayoría de las tareas pendientes?
     */
    @Query(value = "SELECT s.id AS sectorId, s.name AS sectorName, COUNT(t.id) AS taskCount " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "WHERE t.status IN ('vigente', 'atrasado') " +
            "GROUP BY s.id, s.name " +
            "ORDER BY taskCount DESC", nativeQuery = true)
    List<SectorCountProjection> findSectorsWithMostPendingTasks();

    /**
     * 6. ¿Cuántas tareas ha realizado cada usuario por sector?
     */
    @Query(value = "SELECT u.id AS userId, u.username AS userName, s.id AS sectorId, s.name AS sectorName, COUNT(t.id) AS taskCount " +
            "FROM tasks t " +
            "JOIN users u ON t.user_id = u.id " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "WHERE t.status IN ('completado', 'completadoAtrasado') " +
            "GROUP BY u.id, u.username, s.id, s.name " +
            "ORDER BY u.id ASC, taskCount DESC", nativeQuery = true)
    List<UserSectorCountProjection> countCompletedTasksForEachUserPerSector();

    /**
     * 7. ¿Cuál es el sector con más tareas completadas dentro de un radio de 5 km desde la ubicación del usuario?
     */
    @Query(value = "SELECT s.id AS sectorId, s.name AS sectorName, COUNT(t.id) AS taskCount " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "JOIN users u ON t.user_id = u.id " + // <- Cambio aquí
            "WHERE u.id = :userId " +             // <- Filtramos por tu usuario
            "AND t.status IN ('completado', 'completadoAtrasado') " +
            "AND ST_DWithin(s.geo_location::geography, u.geo_location::geography, 5000) " +
            "GROUP BY s.id, s.name " +
            "ORDER BY taskCount DESC " +
            "LIMIT 1", nativeQuery = true)
    SectorCountProjection findTopSectorCompletedWithin5Km(@Param("userId") Long userId);
    /**
     * 8. ¿Cuál es el promedio de distancia entre las tareas completadas y el punto registrado del usuario?
     */
    @Query(value = "SELECT AVG(ST_Distance(s.geo_location::geography, u.geo_location::geography)) AS averageDistance " +
            "FROM tasks t " +
            "JOIN sectors s ON t.sector_id = s.id " +
            "CROSS JOIN users u " +
            "WHERE u.id = :userId AND t.status IN ('completado', 'completadoAtrasado')", nativeQuery = true)
    DistanceProjection getAverageDistanceOfAllCompletedTasksToUser(@Param("userId") Long userId);

}