package Host_Usach_Cloud.Backend.Repository;


import Host_Usach_Cloud.Backend.Entity.Instance;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class InstanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public InstanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Instance save(Instance instance) {
        String sql = "INSERT INTO instance (Name, Ram_id, Cpu_id, Started_at, Storage_id, Terminated, State, User_id, Region_id, Container_id, Active_hours, Ip_address, Color) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //Para guardar la id de retorno del "Statement.RETURN_GENERATED_KEYS"
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            // El preparedStatement es seguridad, para evitar inyecciones SQL
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, instance.getName());
            ps.setLong(2, instance.getRam_id());
            ps.setLong(3, instance.getCpu_id());
            ps.setTimestamp(4, Timestamp.valueOf(instance.getStarted_at()));
            ps.setLong(5, instance.getStorage_id());
            ps.setBoolean(6, instance.isTerminated());
            ps.setString(7, instance.getState());
            ps.setLong(8, instance.getUser_id());
            ps.setLong(9, instance.getRegion_id());
            ps.setString(10, instance.getContainer_id());
            ps.setLong(11, instance.getActive_hours().toHours());
            ps.setString(12, instance.getIp_address());
            ps.setString(13, instance.getColor());
            return ps;
        }, keyHolder);

        // Recuperar el ID generado por la base de datos y asignárselo a la entidad
        if (keyHolder.getKey() != null) {
            instance.setInstance_id(keyHolder.getKey().longValue());
        }

        return instance;
    }


    public Optional<Instance> findById(Long id) {

        String sql = "SELECT * FROM instance WHERE Instance_id = ?";

        try {
            Instance instance = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapInstance(rs), id);
            return Optional.ofNullable(instance);
        } catch (EmptyResultDataAccessException e) {
            // Si la consulta no devuelve nada, retornamos un Optional vacío
            return Optional.empty();
        }
    }

    public List<Instance> findAll() {
        String sql = "SELECT * FROM instance";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs));
    }

    public List<Instance> findAllByUserId(Long userId) {
        String sql = "SELECT * FROM instance WHERE User_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs), userId);
    }

    public List<Instance> findAllByState(String state) {
        String sql = "SELECT * FROM instance WHERE State = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs), state);
    }

    public boolean update(Instance instance) {
        String sql = "UPDATE instance SET Name = ?, Ram_id = ?, Cpu_id = ?, Started_at = ?, Storage_id = ?, " +
                "Terminated = ?, State = ?, User_id = ?, Region_id = ?, Container_id = ?, Active_hours = ?, " +
                "Ip_address = ?, Color = ? WHERE Instance_id = ?";

        Timestamp startedAt = instance.getStarted_at() != null
                ? Timestamp.valueOf(instance.getStarted_at())
                : null;
        long activeHours = instance.getActive_hours() != null
                ? instance.getActive_hours().toHours()
                : 0L;

        int updated = jdbcTemplate.update(sql,
                instance.getName(),
                instance.getRam_id(),
                instance.getCpu_id(),
                startedAt,
                instance.getStorage_id(),
                instance.isTerminated(),
                instance.getState(),
                instance.getUser_id(),
                instance.getRegion_id(),
                instance.getContainer_id(),
                activeHours,
                instance.getIp_address(),
                instance.getColor(),
                instance.getInstance_id());

        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM instance WHERE Instance_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    private Instance mapInstance(ResultSet rs) throws SQLException {
        Timestamp startedAtTs = rs.getTimestamp("Started_at");
        return Instance.builder()
                .Instance_id(rs.getLong("Instance_id"))
                .Name(rs.getString("Name"))
                .Ram_id(rs.getLong("Ram_id"))
                .Cpu_id(rs.getLong("Cpu_id"))
                .Started_at(startedAtTs != null ? startedAtTs.toLocalDateTime() : null)
                .Storage_id(rs.getLong("Storage_id"))
                .Terminated(rs.getBoolean("Terminated"))
                .State(rs.getString("State"))
                .User_id(rs.getLong("User_id"))
                .Region_id(rs.getLong("Region_id"))
                .Container_id(rs.getString("Container_id"))
                .Active_hours(Duration.ofHours(rs.getLong("Active_hours")))
                .Ip_address(rs.getString("Ip_address"))
                .Color(rs.getString("Color"))
                .build();
    }

    

}
