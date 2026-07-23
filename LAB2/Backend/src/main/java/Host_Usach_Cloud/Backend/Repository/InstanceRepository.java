package Host_Usach_Cloud.Backend.Repository;


import Host_Usach_Cloud.Backend.Entity.Instance;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class InstanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public InstanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Instance save(Instance instance) {
        String sql = "INSERT INTO \"Instance\" (\"Name\", \"Ram_id\", \"Cpu_id\", \"Started_at\", \"Storage_id\", \"Terminated\", \"State\", \"User_id\", \"Region_id\", \"Datacenter_id\", \"Container_id\", \"Active_hours\", \"Ip_address\", \"Color\") " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (? || ' seconds')::interval, ?, ?) " +
            "RETURNING \"Instance_id\"";

        long activeSeconds = instance.getActive_hours() != null ? instance.getActive_hours().getSeconds() : 0L;
        Long generatedId = jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) -> rs.getLong(1),
            instance.getName(),
            instance.getRam_id(),
            instance.getCpu_id(),
            instance.getStarted_at() != null ? Timestamp.valueOf(instance.getStarted_at()) : null,
            instance.getStorage_id(),
            instance.isTerminated(),
            instance.getState(),
            instance.getUser_id(),
            instance.getRegion_id(),
            instance.getDatacenter_id(),
            instance.getContainer_id(),
            activeSeconds,
            instance.getIp_address(),
            instance.getColor()
        );

        if (generatedId != null) {
            instance.setInstance_id(generatedId);
        }

        return instance;
    }


    public Optional<Instance> findById(Long id) {

        String sql = "SELECT * FROM \"Instance\" WHERE \"Instance_id\" = ?";

        try {
            Instance instance = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapInstance(rs), id);
            return Optional.ofNullable(instance);
        } catch (EmptyResultDataAccessException e) {
            // Si la consulta no devuelve nada, retornamos un Optional vacío
            return Optional.empty();
        }
    }

    public List<Instance> findAll() {
        String sql = "SELECT * FROM \"Instance\"";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs));
    }

    public List<Instance> findAllByUserId(Long userId) {
        String sql = "SELECT * FROM \"Instance\" WHERE \"User_id\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs), userId);
    }

    public List<Instance> findAllByState(String state) {
        String sql = "SELECT * FROM \"Instance\" WHERE \"State\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInstance(rs), state);
    }

    public boolean update(Instance instance) {
        String sql = "UPDATE \"Instance\" SET \"Name\" = ?, \"Ram_id\" = ?, \"Cpu_id\" = ?, \"Started_at\" = ?, \"Storage_id\" = ?, " +
            "\"Terminated\" = ?, \"State\" = ?, \"User_id\" = ?, \"Region_id\" = ?, \"Datacenter_id\" = ?, \"Container_id\" = ?, \"Active_hours\" = (? || ' seconds')::interval, " +
            "\"Ip_address\" = ?, \"Color\" = ? WHERE \"Instance_id\" = ?";

        Timestamp startedAt = instance.getStarted_at() != null
                ? Timestamp.valueOf(instance.getStarted_at())
                : null;
        long activeSeconds = instance.getActive_hours() != null
            ? instance.getActive_hours().getSeconds()
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
                instance.getDatacenter_id(),
                instance.getContainer_id(),
                activeSeconds,
                instance.getIp_address(),
                instance.getColor(),
                instance.getInstance_id());

        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"Instance\" WHERE \"Instance_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public void provisionInstance(String ipAddress, Long instanceId) {
        String sql = "CALL provision_instance(?, ?)";
        jdbcTemplate.update(sql, ipAddress, instanceId);
    }

    private Instance mapInstance(ResultSet rs) throws SQLException {
        Timestamp startedAtTs = rs.getTimestamp("Started_at");
        String intervalStr = rs.getString("Active_hours");
        Duration activeHours = parsePgInterval(intervalStr);
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
                .Datacenter_id(rs.getLong("Datacenter_id"))
                .Container_id(rs.getString("Container_id"))
                .Active_hours(activeHours)
                .Ip_address(rs.getString("Ip_address"))
                .Color(rs.getString("Color"))
                .build();
    }
/*
    private Duration parsePgInterval(String intervalStr) {
        if (intervalStr == null || intervalStr.isBlank()) {
            return Duration.ZERO;
        }
        try {
            String[] parts = intervalStr.split(":");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }
*/

    private Duration parsePgInterval(String intervalStr) {
        if (intervalStr == null || intervalStr.isBlank()) {
            return Duration.ZERO;
        }

        String normalized = intervalStr.trim();
        long days = 0L;

        Matcher dayMatcher = Pattern.compile("^(\\d+)\\s+day[s]?\\b").matcher(normalized);
        if (dayMatcher.find()) {
            days = Long.parseLong(dayMatcher.group(1));
            normalized = normalized.substring(dayMatcher.end()).trim();
        }

        Matcher timeMatcher = Pattern.compile("^(?:(\\d+):)?(\\d+):(\\d+)(?:\\.(\\d+))?$").matcher(normalized);
        if (!timeMatcher.matches()) {
            return Duration.ZERO;
        }

        long hours = timeMatcher.group(1) != null ? Long.parseLong(timeMatcher.group(1)) : 0L;
        long minutes = Long.parseLong(timeMatcher.group(2));
        long seconds = Long.parseLong(timeMatcher.group(3));

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

}
