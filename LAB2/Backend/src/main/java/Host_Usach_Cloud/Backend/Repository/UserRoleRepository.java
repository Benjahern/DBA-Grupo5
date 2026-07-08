package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.User_role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public User_role save(User_role userRole) {
        String sql = "INSERT INTO \"User_role\" (\"User_id\", \"Role_id\") VALUES (?, ?)";

        jdbcTemplate.update(sql, userRole.getUser_id(), userRole.getRole_id());

        return userRole;
    }

    public Set<String> findRoleNamesByUserId(Long userId) {
        String sql = "SELECT r.\"Role\" FROM \"Role\" r " +
                     "JOIN \"User_role\" ur ON r.\"Role_id\" = ur.\"Role_id\" " +
                     "WHERE ur.\"User_id\" = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("Role"), userId));
    }
}
