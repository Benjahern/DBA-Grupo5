package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Ip;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class IpRepository {

    private final JdbcTemplate jdbcTemplate;

    public IpRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Ip save(Ip ip) {
        String sql = "INSERT INTO \"Ip\" (\"Ip_address\", \"Used\") VALUES (?, ?) RETURNING \"Ip_id\"";
        Long id = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getLong(1), ip.getIp_address(), ip.isUsed());
        ip.setIp_id(id);
        return ip;
    }

    public Optional<Ip> findById(Long id) {
        String sql = "SELECT * FROM \"Ip\" WHERE \"Ip_id\" = ?";
        try {
            Ip ip = jdbcTemplate.queryForObject(sql, this::mapIp, id);
            return Optional.ofNullable(ip);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Ip> findByAddress(String address) {
        String sql = "SELECT * FROM \"Ip\" WHERE \"Ip_address\" = ?";
        try {
            Ip ip = jdbcTemplate.queryForObject(sql, this::mapIp, address);
            return Optional.ofNullable(ip);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean updateUsed(Long id, boolean used) {
        String sql = "UPDATE \"Ip\" SET \"Used\" = ? WHERE \"Ip_id\" = ?";
        return jdbcTemplate.update(sql, used, id) > 0;
    }

    private Ip mapIp(ResultSet rs) throws SQLException {
        return Ip.builder()
                .Ip_id(rs.getLong("Ip_id"))
                .Ip_address(rs.getString("Ip_address"))
                .Used(rs.getBoolean("Used"))
                .build();
    }
}