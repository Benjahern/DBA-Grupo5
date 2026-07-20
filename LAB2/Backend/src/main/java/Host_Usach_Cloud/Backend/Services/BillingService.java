package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Entity.Instance;
import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Entity.Region;
import Host_Usach_Cloud.Backend.Entity.Storage;
import Host_Usach_Cloud.Backend.Repository.CpuRepository;
import Host_Usach_Cloud.Backend.Repository.InstanceRepository;
import Host_Usach_Cloud.Backend.Repository.RamRepository;
import Host_Usach_Cloud.Backend.Repository.RegionRepository;
import Host_Usach_Cloud.Backend.Repository.StorageRepository;
import Host_Usach_Cloud.Backend.Services.DTO.BillingDistanceResult;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BillingService {

    private final JdbcTemplate jdbcTemplate;
    private final InstanceRepository instanceRepository;
    private final RegionRepository regionRepository;
    private final CpuRepository cpuRepository;
    private final RamRepository ramRepository;
    private final StorageRepository storageRepository;

    public BillingService(JdbcTemplate jdbcTemplate,
                          InstanceRepository instanceRepository,
                          RegionRepository regionRepository,
                          CpuRepository cpuRepository,
                          RamRepository ramRepository,
                          StorageRepository storageRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.instanceRepository = instanceRepository;
        this.regionRepository = regionRepository;
        this.cpuRepository = cpuRepository;
        this.ramRepository = ramRepository;
        this.storageRepository = storageRepository;
    }

    public void generateMonthlyTickets(Long userId) {
        String sql = "CALL generate_monthly_tickets(?)";
        jdbcTemplate.update(sql, userId);
    }

    public BillingDistanceResult calculateDistanceBilling(Long instanceId, double userLat, double userLon) {
        Instance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found: " + instanceId));

        CPU cpu = cpuRepository.findById(instance.getCpu_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CPU not found"));
        Ram ram = ramRepository.findById(instance.getRam_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RAM not found"));
        Storage storage = storageRepository.findById(instance.getStorage_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage not found"));
        Region region = regionRepository.findById(instance.getRegion_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found"));

        double totalCostPh = cpu.getCost_ph() + ram.getCost_ph() + storage.getCost_ph();
        double activeHours = instance.getActive_hours() != null
                ? instance.getActive_hours().toSeconds() / 3600.0
                : 0.0;
        BigDecimal baseCost = BigDecimal.valueOf(totalCostPh * activeHours);

        // getCentroid() returns [lon, lat] — GeoJSON order (X=longitude, Y=latitude)
        double[] centroid = region.getCentroid();
        if (centroid == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Region has no geometry");
        }
        double regionLat = centroid[1];
        double regionLon = centroid[0];

        double distanceKm = haversineKm(userLat, userLon, regionLat, regionLon);
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
