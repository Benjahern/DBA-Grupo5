package Host_Usach_Cloud.Backend.Repository;


import Host_Usach_Cloud.Backend.Entity.Instance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

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

}
