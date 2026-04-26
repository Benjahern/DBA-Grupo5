package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.User_role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public User_role save(User_role userRole) {
        String sql = "INSERT INTO \"User_role\" (\"User_id\", \"Role_id\") VALUES (?, ?)";
        
        jdbcTemplate.update(sql, userRole.getUser_id(), userRole.getRole_id());
        
        return userRole;
    }
}
