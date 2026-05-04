package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Region;
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
public class RegionRepository {
    private final JdbcTemplate jdbcTemplate;

    public RegionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Region save(Region region) {
        String sql = "INSERT INTO \"Region\" (\"Name\", \"Map_top\", \"Map_left\") VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, region.getName());
            if (region.getMap_top() != null) {
                ps.setDouble(2, region.getMap_top());
            } else {
                ps.setNull(2, java.sql.Types.REAL);
            }
            if (region.getMap_left() != null) {
                ps.setDouble(3, region.getMap_left());
            } else {
                ps.setNull(3, java.sql.Types.REAL);
            }
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object key = keyHolder.getKeys().values().iterator().next();
            if (key instanceof Number) {
                region.setRegion_id(((Number) key).longValue());
            }
        }

        return region;
    }

    public Optional<Region> findById(Long id) {
        String sql = "SELECT * FROM \"Region\" WHERE \"Region_id\" = ?";

        try {
            Region region = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            Region.builder()
                                    .Region_id(rs.getLong("Region_id"))
                                    .Name(rs.getString("Name"))
                                    .Map_top(rs.getObject("map_top") != null ? rs.getDouble("map_top") : null)
                                    .Map_left(rs.getObject("map_left") != null ? rs.getDouble("map_left") : null)
                                    .build()
                    , id);
            return Optional.ofNullable(region);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Region> findAll() {
        String sql = "SELECT * FROM \"Region\"";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        Region.builder()
                                    .Region_id(rs.getLong("Region_id"))
                                    .Name(rs.getString("Name"))
                                    .Map_top(rs.getObject("map_top") != null ? rs.getDouble("map_top") : null)
                                    .Map_left(rs.getObject("map_left") != null ? rs.getDouble("map_left") : null)
                                    .build());
    }

    public boolean update(Region region) {
        String sql = "UPDATE \"Region\" SET \"Name\" = ?, \"Map_top\" = ?, \"Map_left\" = ? WHERE \"Region_id\" = ?";
        return jdbcTemplate.update(sql, region.getName(), region.getMap_top(), region.getMap_left(), region.getRegion_id()) > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"Region\" WHERE \"Region_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
