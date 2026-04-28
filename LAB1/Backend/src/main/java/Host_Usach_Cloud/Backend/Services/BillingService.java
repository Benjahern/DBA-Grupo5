package Host_Usach_Cloud.Backend.Services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final JdbcTemplate jdbcTemplate;

    public BillingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void generateMonthlyTickets(Long userId) {
        String sql = "CALL generate_monthly_tickets(?)";
        jdbcTemplate.update(sql, userId);
    }
}
