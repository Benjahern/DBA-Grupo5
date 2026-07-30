package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Consumption;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.InstanceMongoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ConsumptionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final InstanceMongoRepository instanceMongoRepository;

    public ConsumptionRepository(JdbcTemplate jdbcTemplate,
                                 InstanceMongoRepository instanceMongoRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.instanceMongoRepository = instanceMongoRepository;
    }

    public void save(Consumption consumption) {
        String sql = "INSERT INTO \"Consumption\" (\"Cpu_stats\", \"Ram_stats\", \"Storage_stats\", \"Instance_id\", \"Created_at\") " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                consumption.getCpu_stats(),
                consumption.getRam_stats(),
                consumption.getStorage_stats(),
                consumption.getInstance_id(),
                consumption.getCreated_at());
    }

    /**
     * El JOIN Consumption↔Instance ya no existe en Postgres (Instance vive en
     * Mongo). El filtro por usuario se hace en app:
     *   1) Traemos los numericIds del usuario desde Mongo.
     *   2) Agregamos en Postgres con WHERE Instance_id IN (...).
     */
    public MonthlyConsumptionStats findMonthlyStatsByUser(Long userId) {
        List<Long> instanceIds = instanceMongoRepository.findByUserId(userId)
                .stream()
                .map(InstanceDocument::getNumericId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (instanceIds.isEmpty()) {
            return new MonthlyConsumptionStats(0, 0, 0, 0);
        }

        String placeholders = String.join(",", Collections.nCopies(instanceIds.size(), "?"));
        String sql = "SELECT " +
                "COALESCE(SUM(\"Cpu_stats\"), 0) AS cpu_sum, " +
                "COALESCE(SUM(\"Ram_stats\"), 0) AS ram_sum, " +
                "COALESCE(SUM(\"Storage_stats\"), 0) AS storage_sum, " +
                "COUNT(*) AS samples " +
                "FROM \"Consumption\" " +
                "WHERE \"Instance_id\" IN (" + placeholders + ") " +
                "AND \"Created_at\" >= date_trunc('month', CURRENT_DATE) " +
                "AND \"Created_at\" < (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new MonthlyConsumptionStats(
                rs.getDouble("cpu_sum"),
                rs.getDouble("ram_sum"),
                rs.getDouble("storage_sum"),
                rs.getInt("samples")
        ), instanceIds.toArray());
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

        public double getCpuSum() { return cpuSum; }
        public double getRamSum() { return ramSum; }
        public double getStorageSum() { return storageSum; }
        public int getSamples() { return samples; }
    }
}