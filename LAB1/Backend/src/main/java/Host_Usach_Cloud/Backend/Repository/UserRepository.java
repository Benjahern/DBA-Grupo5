package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public Users save(Users user) {
        String sql = "INSERT INTO \"Users\" (\"Email\", \"Name\", \"Max_instances\", \"Lock\") VALUES (?, ?, ?, ?)";
        
        //Lo mismo que el instance
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getName());
            ps.setInt(3, user.getMax_instances());
            ps.setBoolean(4, user.isLock());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            Number id = (Number) keyHolder.getKeys().get("User_id");
            if (id != null) {
                user.setUser_id(id.longValue());
            }
        }
        return user;
    }

    public Optional<Users> findByEmail(String email) {
        String sql = "SELECT * FROM \"Users\" WHERE \"Email\" = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Users.builder()
                .User_id(rs.getLong("User_id"))
                .Email(rs.getString("Email"))
                .Name(rs.getString("Name"))
            .Max_instances(rs.getInt("Max_instances"))
                .Lock(rs.getBoolean("Lock"))
                .build(), email).stream().findFirst();
    }
}
