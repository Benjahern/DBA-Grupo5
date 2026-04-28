package Host_Usach_Cloud.Backend.Repository;


import Host_Usach_Cloud.Backend.Entity.Ram;
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
public class RamRepository {

    private final JdbcTemplate jdbcTemplate;

    public RamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Ram save(Ram ram) {
        String sql = "INSERT INTO ram (Quantity, Cost_ph) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ram.getQuantity());
            ps.setFloat(2, ram.getCost_ph());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            ram.setRam_id(keyHolder.getKey().longValue());
        }

        return ram;
    }

    public Optional<Ram> findById(Long id) {
        String sql = "SELECT * FROM ram WHERE Ram_id = ?";

        try {
            Ram ram = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            Ram.builder()
                                    .Ram_id(rs.getLong("Ram_id"))
                                    .Quantity(rs.getInt("Quantity"))
                                    .Cost_ph(rs.getFloat("Cost_ph"))
                                    .build()
                    , id);
            return Optional.ofNullable(ram);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Ram> findAll() {
        String sql = "SELECT * FROM ram";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Ram.builder()
                .Ram_id(rs.getLong("Ram_id"))
                .Quantity(rs.getInt("Quantity"))
                .Cost_ph(rs.getFloat("Cost_ph"))
                .build());
    }

    public boolean update(Ram ram) {
        String sql = "UPDATE ram SET Quantity = ?, Cost_ph = ? WHERE Ram_id = ?";
        int updated = jdbcTemplate.update(sql, ram.getQuantity(), ram.getCost_ph(), ram.getRam_id());
        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM ram WHERE Ram_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

}
