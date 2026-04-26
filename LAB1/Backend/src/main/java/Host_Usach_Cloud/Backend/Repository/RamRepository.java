package Host_Usach_Cloud.Backend.Repository;


import Host_Usach_Cloud.Backend.Entity.Ram;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RamRepository {

    private final JdbcTemplate jdbcTemplate;

    public RamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

}
