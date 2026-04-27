package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Role> findByName(String roleName) {
        String sql = "SELECT * FROM \"Role\" WHERE \"Role\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Role.builder()
                .Role_id(rs.getLong("Role_id"))
                .Role(rs.getString("Role"))
                .build(), roleName).stream().findFirst();
    }

    public Optional<Role> findById(Long roleId) {
        String sql = "SELECT * FROM \"Role\" WHERE \"Role_id\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Role.builder()
                .Role_id(rs.getLong("Role_id"))
                .Role(rs.getString("Role"))
                .build(), roleId).stream().findFirst();
    }
}
