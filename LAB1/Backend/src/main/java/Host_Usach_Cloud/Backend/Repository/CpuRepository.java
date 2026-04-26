package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.CPU;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CpuRepository {

    private final JdbcTemplate jdbcTemplate;

    public CpuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<CPU> findById(Long id) {
        String sql = "SELECT * FROM cpu WHERE Cpu_id = ?";

        try {
            CPU cpu = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            CPU.builder()
                                    .Cpu_id(rs.getLong("Cpu_id"))
                                    .Quantity(rs.getInt("Quantity"))
                                    .Cost_ph(rs.getFloat("Cost_ph"))
                                    .build()
                    , id);
            return Optional.ofNullable(cpu);
        } catch (EmptyResultDataAccessException e) {
            // Si la consulta no devuelve nada, retornamos un Optional vacío
            return Optional.empty();
        }
    }
}
