package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.CPU;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CpuRepository {

    private final JdbcTemplate jdbcTemplate;

    public CpuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CPU save(CPU cpu) {
        String sql = "INSERT INTO cpu (Quantity, Cost_ph) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cpu.getQuantity());
            ps.setFloat(2, cpu.getCost_ph());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            cpu.setCpu_id(keyHolder.getKey().longValue());
        }

        return cpu;
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

    public List<CPU> findAll() {
        String sql = "SELECT * FROM cpu";
        return jdbcTemplate.query(sql, (rs, rowNum) -> CPU.builder()
                .Cpu_id(rs.getLong("Cpu_id"))
                .Quantity(rs.getInt("Quantity"))
                .Cost_ph(rs.getFloat("Cost_ph"))
                .build());
    }

    public boolean update(CPU cpu) {
        String sql = "UPDATE cpu SET Quantity = ?, Cost_ph = ? WHERE Cpu_id = ?";
        int updated = jdbcTemplate.update(sql, cpu.getQuantity(), cpu.getCost_ph(), cpu.getCpu_id());
        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM cpu WHERE Cpu_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
