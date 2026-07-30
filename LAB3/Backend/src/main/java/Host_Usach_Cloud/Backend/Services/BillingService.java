package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.*;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.InstanceMongoRepository;
import Host_Usach_Cloud.Backend.Repository.*;
import Host_Usach_Cloud.Backend.Services.DTO.BillingDistanceResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BillingService {

    private final JdbcTemplate jdbcTemplate;
    private final InstanceMongoRepository instanceMongoRepository;
    private final MongoTemplate mongoTemplate;
    private final CpuRepository cpuRepository;
    private final RamRepository ramRepository;
    private final StorageRepository storageRepository;
    private final DatacenterRepository datacenterRepository;

    public BillingService(JdbcTemplate jdbcTemplate,
                          InstanceMongoRepository instanceMongoRepository,
                          MongoTemplate mongoTemplate,
                          CpuRepository cpuRepository,
                          RamRepository ramRepository,
                          StorageRepository storageRepository,
                          DatacenterRepository datacenterRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.instanceMongoRepository = instanceMongoRepository;
        this.mongoTemplate = mongoTemplate;
        this.cpuRepository = cpuRepository;
        this.ramRepository = ramRepository;
        this.storageRepository = storageRepository;
        this.datacenterRepository = datacenterRepository;
    }

    public void generateMonthlyTickets(Long userId) {
        String sql = "CALL generate_monthly_tickets(?)";
        jdbcTemplate.update(sql, userId);
    }

    public BillingDistanceResult calculateDistanceBilling(Long numericId, double userLat, double userLon) {
        InstanceDocument instance = mongoTemplate.findOne(
                Query.query(Criteria.where("numericId").is(numericId)),
                InstanceDocument.class, "instances");
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found: " + numericId);
        }
        CPU cpu = cpuRepository.findById(instance.getCpuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CPU not found"));
        Ram ram = ramRepository.findById(instance.getRamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RAM not found"));
        Storage storage = storageRepository.findById(instance.getStorageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage not found"));
        Datacenter datacenter = datacenterRepository.findById(instance.getDatacenterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Datacenter not found"));

        double totalCostPh = cpu.getCost_ph() + ram.getCost_ph() + storage.getCost_ph();
        double activeHours = instance.getActiveHours() != null
                ? instance.getActiveHours()
                : 0.0;
        BigDecimal baseCost = BigDecimal.valueOf(totalCostPh * activeHours);

        double datacenterLat = datacenter.getLatitude();
        double datacenterLon = datacenter.getLongitude();

        double distanceKm = haversineKm(userLat, userLon, datacenterLat, datacenterLon);
        double rate = surchargeRate(distanceKm);

        BigDecimal surcharge = baseCost.multiply(BigDecimal.valueOf(rate));
        BigDecimal total = baseCost.add(surcharge).setScale(2, RoundingMode.HALF_UP);

        return new BillingDistanceResult(
                baseCost.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(rate),
                surcharge.setScale(2, RoundingMode.HALF_UP),
                total
        );
    }

    public static double surchargeRate(double distanceKm) {
        if (distanceKm <= 200) return 0.00;
        if (distanceKm <= 1000) return 0.05;
        if (distanceKm <= 3000) return 0.12;
        return 0.20;
    }

    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}