package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Datacenter;
import Host_Usach_Cloud.Backend.Services.DTO.CoordinateDTO;
import Host_Usach_Cloud.Backend.Services.DTO.DatacenterDistance;
import Host_Usach_Cloud.Backend.Services.DTO.LocationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatacenterRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Datacenter> rowMapper = (rs, rowNum) ->
            Datacenter.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .status(
                            Datacenter.DatacenterStatus.valueOf(
                                    rs.getString("status")
                            )
                    )
                    .currentInstances(rs.getInt("current_instances"))
                    .capacity(rs.getInt("capacity"))
                    .latitude(rs.getDouble("latitude"))
                    .longitude(rs.getDouble("longitude"))
                    .regionId(rs.getLong("region_id"))
                    .riskZoneId(rs.getLong("risk_zone_id"))
                    .build();

    private final RowMapper<LocationResponse> locationResponseRowMapper =
            (rs, rowNum) -> LocationResponse.builder()
                    .regionId(rs.getLong("region_id"))
                    .regionName(rs.getString("region_name"))
                    .riskZoneId(rs.getLong("risk_zone_id"))
                    .riskZoneName(rs.getString("risk_zone_name"))
                    .build();

    public Long create(Datacenter datacenter) {

        String sql = """
        INSERT INTO "Datacenter"
        (
            name,
            status,
            current_instances,
            capacity,
            latitude,
            longitude,
            region_id,
            risk_zone_id
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                datacenter.getName(),
                datacenter.getStatus().name(),
                datacenter.getCurrentInstances(),
                datacenter.getCapacity(),
                datacenter.getLatitude(),
                datacenter.getLongitude(),
                datacenter.getRegionId(),
                datacenter.getRiskZoneId()
        );
    }

    public List<Datacenter> findAll() {

        String sql = """
        SELECT *
        FROM "Datacenter"
        ORDER BY id
        """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Datacenter> findById(Long id) {

        String sql = """
        SELECT *
        FROM "Datacenter"
        WHERE id = ?
        """;

        List<Datacenter> results =
                jdbcTemplate.query(
                        sql,
                        rowMapper,
                        id
                );

        return results.stream().findFirst();
    }

    public int update(Long id, Datacenter datacenter) {

        String sql = """
        UPDATE "Datacenter"
        SET
            name = ?,
            status = ?,
            current_instances = ?,
            capacity = ?,
            latitude = ?,
            longitude = ?,
            region_id = ?,
            risk_zone_id = ?
        WHERE id = ?
        """;

        return jdbcTemplate.update(
                sql,
                datacenter.getName(),
                datacenter.getStatus().name(),
                datacenter.getCurrentInstances(),
                datacenter.getCapacity(),
                datacenter.getLatitude(),
                datacenter.getLongitude(),
                datacenter.getRegionId(),
                datacenter.getRiskZoneId(),
                id
        );
    }

    public int delete(Long id) {

        String sql = """
        DELETE FROM "Datacenter"
        WHERE id = ?
        """;

        return jdbcTemplate.update(
                sql,
                id
        );
    }

    public Optional<LocationResponse> findRegionByCoordinates(
            Double latitude,
            Double longitude
    ) {

        String sql = """
        SELECT
            "Region_id" AS region_id,
            "Name" AS region_name
        FROM "Region"
        WHERE ST_Intersects(
            "Geom",
            ST_SetSRID(
                ST_Point(?, ?),
                4326
            )
        )
        LIMIT 1
        """;

        List<LocationResponse> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> LocationResponse.builder()
                                .regionId(rs.getLong("region_id"))
                                .regionName(rs.getString("region_name"))
                                .build(),
                        longitude,
                        latitude
                );

        return results.stream().findFirst();
    }

    public Optional<LocationResponse> findRiskZoneByCoordinates(
            Double latitude,
            Double longitude
    ) {

        String sql = """
        SELECT
            ogc_fid AS risk_zone_id,
            platename AS risk_zone_name
        FROM riskzone
        WHERE ST_Intersects(
            wkb_geometry,
            ST_SetSRID(
                ST_Point(?, ?),
                4326
            )
        )
        LIMIT 1
        """;

        List<LocationResponse> results =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> LocationResponse.builder()
                                .riskZoneId(rs.getLong("risk_zone_id"))
                                .riskZoneName(rs.getString("risk_zone_name"))
                                .build(),
                        longitude,
                        latitude
                );

        return results.stream().findFirst();
    }

    public Optional<CoordinateDTO> findInstanceCentroid(Long instanceId) {

        String sql = """
        SELECT
            ST_Y(
                ST_Centroid(r."Geom")
            ) AS latitude,

            ST_X(
                ST_Centroid(r."Geom")
            ) AS longitude

        FROM "Instance" i

        JOIN "Region" r
            ON i."Region_id" = r."Region_id"

        WHERE i."Instance_id" = ?
        """;

        List<CoordinateDTO> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> CoordinateDTO.builder()
                        .latitude(rs.getDouble("latitude"))
                        .longitude(rs.getDouble("longitude"))
                        .build(),
                instanceId
        );

        return results.stream().findFirst();
    }

    public List<DatacenterDistance> findClosestDatacenters(
            Double latitude,
            Double longitude,
            Long excludedRiskZoneId
    ) {

        String sql = """
        
                SELECT
            d.id,
            d.name,
            d.latitude,
            d.longitude,
            d.risk_zone_id,
        
            ST_Distance(
                ST_SetSRID(
                    ST_Point(d.longitude, d.latitude),
                    4326
                )::geography,
        
                ST_SetSRID(
                    ST_Point(?, ?),
                    4326
                )::geography
            ) / 1000.0 AS distance_km
        
        FROM "Datacenter" d
        
        WHERE d.risk_zone_id <> ?
        
        ORDER BY distance_km
        
        LIMIT 3
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> DatacenterDistance.builder()
                        .datacenterId(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .latitude(rs.getDouble("latitude"))
                        .longitude(rs.getDouble("longitude"))
                        .riskZoneId(rs.getLong("risk_zone_id"))
                        .distanceKm(rs.getDouble("distance_km"))
                        .build(),
                longitude,
                latitude,
                excludedRiskZoneId
        );
    }

}