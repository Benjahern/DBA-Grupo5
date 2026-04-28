package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Consumption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsumptionRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConsumptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Consumption consumption) {
        String sql = "INSERT INTO \"Consumption\" (\"Cpu_stats\", \"Ram_stats\", \"Storage_stats\", \"Instance_id\", \"Created_at\") VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                consumption.getCpu_stats(),
                consumption.getRam_stats(),
                consumption.getStorage_stats(),
                consumption.getInstance_id(),
                consumption.getCreated_at());
    }

    public MonthlyConsumptionStats findMonthlyStatsByUser(Long userId) {
        String sql = "SELECT " +
            "COALESCE(SUM(c.\"Cpu_stats\"), 0) AS cpu_sum, " +
            "COALESCE(SUM(c.\"Ram_stats\"), 0) AS ram_sum, " +
            "COALESCE(SUM(c.\"Storage_stats\"), 0) AS storage_sum, " +
            "COUNT(*) AS samples " +
            "FROM \"Consumption\" c " +
            "JOIN \"Instance\" i ON c.\"Instance_id\" = i.\"Instance_id\" " +
            "WHERE i.\"User_id\" = ? " +
            "AND c.\"Created_at\" >= date_trunc('month', CURRENT_DATE) " +
            "AND c.\"Created_at\" < (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new MonthlyConsumptionStats(
                rs.getDouble("cpu_sum"),
                rs.getDouble("ram_sum"),
                rs.getDouble("storage_sum"),
                rs.getInt("samples")
        ), userId);
    }

    public static class MonthlyConsumptionStats {
        private final double cpuSum;
        private final double ramSum;
        private final double storageSum;
        private final int samples;

        public MonthlyConsumptionStats(double cpuSum, double ramSum, double storageSum, int samples) {
            this.cpuSum = cpuSum;
            this.ramSum = ramSum;
            this.storageSum = storageSum;
            this.samples = samples;
        }

        public double getCpuSum() {
            return cpuSum;
        }

        public double getRamSum() {
            return ramSum;
        }

        public double getStorageSum() {
            return storageSum;
        }

        public int getSamples() {
            return samples;
        }
    }
}
