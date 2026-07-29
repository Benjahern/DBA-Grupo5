package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Region;
import Host_Usach_Cloud.Backend.Services.DTO.PingResult;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
        String sql = "INSERT INTO \"Region\" (\"Name\", \"Geom\") VALUES (?, ST_GeomFromText(?, 4326))";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, region.getName());
            if (region.getGeom() != null) {
                ps.setString(2, region.getGeom().toText());
            } else {
                ps.setNull(2, java.sql.Types.OTHER);
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
        String sql = "SELECT \"Region_id\", \"Name\", ST_AsText(\"Geom\") AS geom_text "
                   + "FROM \"Region\" WHERE \"Region_id\" = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapRow(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Region> findAll() {
        String sql = "SELECT \"Region_id\", \"Name\", ST_AsText(\"Geom\") AS geom_text FROM \"Region\"";
        return jdbcTemplate.query(sql, mapRow());
    }

    public boolean update(Region region) {
        String sql = "UPDATE \"Region\" SET \"Name\" = ?, \"Geom\" = ST_GeomFromText(?, 4326) "
                   + "WHERE \"Region_id\" = ?";
        Polygon g = region.getGeom();
        return jdbcTemplate.update(sql, region.getName(),
                g != null ? g.toText() : null, region.getRegion_id()) > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"Region\" WHERE \"Region_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<PingResult> getLatencyToRegions(double lat, double lng) {
        String sql = "SELECT region_id, region_name, distance_m, latency_rtt_ms " +
                     "FROM fn_latencia_a_regiones(?, ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> PingResult.builder()
                .region_id(rs.getLong("region_id"))
                .region_name(rs.getString("region_name"))
                .distance_m(rs.getDouble("distance_m"))
                .latency_rtt_ms(rs.getDouble("latency_rtt_ms"))
                .build(), lat, lng);
    }

    private RowMapper<Region> mapRow() {
        return (rs, rowNum) -> {
            Region.RegionBuilder b = Region.builder()
                    .Region_id(rs.getLong("Region_id"))
                    .Name(rs.getString("Name"));
            String wkt = rs.getString("geom_text");
            if (wkt != null) {
                try {
                    b.Geom((Polygon) new WKTReader().read(wkt));
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid WKT from DB: " + wkt, e);
                }
            }
            return b.build();
        };
    }
}