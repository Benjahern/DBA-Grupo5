package com.example.Backend.Repository;

import com.example.Backend.Entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    // Por el momento está en JPA

    public List<TaskEntity> findBySectorID(Long sectorID);

    public List<TaskEntity> findIsDone(boolean isDone);

    @Query("SELECT t FROM TaskEntity t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    public List<TaskEntity> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM TaskEntity t WHERE (t.isDone = :isDone) AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    public List<TaskEntity> findByIsDoneAndByKeyword(@Param("isDone")boolean isDone, @Param("keyword") String keyword);

    // El siguiente bloque está comentado por si se quiere usar el JDBC

    /*
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TaskEntity save(TaskEntity task) {
        String sql = "INSERT INTO \"TaskEntity\" (\"title\", \"description\", \"dueDate\", \"userID\", \"sectorID\", \"isDone\") " +
                "VALUES (?, ?, ?, ?, ?, ?)" +
                "RETURNING \"TaskEntity_id\"";

        Long generatedId = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> rs.getLong(1),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getUserID(),
                task.getSectorID(),
                task.getIsDone()
        );

        if (generatedId != null) {
            task.setId(generatedId);
        }

        return task;
    }

    public boolean update(TaskEntity task) {
        String sql = "UPDATE \"TaskEntity\" SET \"title\" = ?, \"description\" = ?, \"dueDate\" = ?, \"userID\" = ?, \"sectorID\" = ?, " +
                "\"isDone\" = ? WHERE \"TaskEntity_id\" = ?";

        int updated = jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getUserID(),
                task.getSectorID(),
                task.getIsDone());

        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"TaskEntity\" WHERE \"TaskEntity_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<TaskEntity> findAll() {
        String sql = "SELECT * FROM \"TaskEntity\"";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTaskEntity(rs));
    }

    public Optional<TaskEntity> findById(Long id) {

        String sql = "SELECT * FROM \"TaskEntity\" WHERE \"TaskEntity_id\" = ?";

        try {
            TaskEntity task = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapTaskEntity(rs), id);
            return Optional.ofNullable(task);
        } catch (EmptyResultDataAccessException e) {
            // Si la consulta no devuelve nada, retornamos un Optional vacío
            return Optional.empty();
        }
    }

    private TaskEntity mapTaskEntity(ResultSet rs) throws SQLException {
        return TaskEntity.builder()
                .id(rs.getLong("TaskEntity_id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .dueDate(rs.getDate("dueDate").toLocalDate())
                .userID(rs.getLong("userID"))
                .sectorID(rs.getLong("sectorID"))
                .isDone(rs.getBoolean("isDone"))
                .build();
    }

    public List<TaskEntity> findIsDone(boolean state) {
        String sql = "SELECT * FROM \"TaskEntity\" WHERE \"isDone\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTaskEntity(rs), state);
    }
*/
}
