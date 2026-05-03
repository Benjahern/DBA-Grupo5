package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Ram;
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
        String sql = "INSERT INTO \"Region\" (\"Name\") VALUES = ?";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, region.getName());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            region.setRegion_id(keyHolder.getKey().longValue());
        }

        return region;
    }

    public Optional<Region> findById(Long id) {
        String sql = "SELECT * FROM \"Ram\" WHERE \"Region_id\" = ?";

        try {
            Region region = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            Region.builder()
                                    .Region_id(rs.getLong("Region_id"))
                                    .Name(rs.getString("Name"))
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
                                    .build());
    }

    public boolean update(Region region) {
        String sql = "UPDATE \"Region\" SET \"Name\" = ? WHERE \"Region_id\" = ?";
        return jdbcTemplate.update(sql, region.getName(), region.getRegion_id()) > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"Region\" WHERE \"Region_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
