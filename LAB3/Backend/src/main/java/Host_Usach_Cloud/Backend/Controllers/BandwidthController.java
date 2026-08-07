package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Mongo.Services.BandwidthAggregationService;
import Host_Usach_Cloud.Backend.Services.DTO.BandwidthBucketReport;
import Host_Usach_Cloud.Backend.Services.DTO.BandwidthCostReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * Controller REST para los endpoints de consumo de ancho de banda.
 * Expone los resultados de los Aggregation Pipelines de MongoDB.
 */
@RestController
@RequestMapping("/api/bandwidth")
public class BandwidthController {

    private final BandwidthAggregationService aggregationService;

    public BandwidthController(BandwidthAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * Pipeline 1 ($group): Consumo de ancho de banda y costo asociado por
     * cliente y periodo de facturación.
     *
     * @param period periodo "YYYY-MM" (default: mes actual)
     * @param userId filtro opcional por usuario
     */
    @GetMapping("/report")
    public ResponseEntity<List<BandwidthCostReport>> getConsumptionReport(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long userId) {

        String resolvedPeriod = period != null ? period : YearMonth.now().toString();
        List<BandwidthCostReport> report = aggregationService.getConsumptionByClientAndPeriod(
                resolvedPeriod, userId);
        return ResponseEntity.ok(report);
    }

    /**
     * Pipeline 2 ($bucket): Distribución de clientes por rangos de consumo
     * de ancho de banda.
     *
     * @param period periodo "YYYY-MM" (default: mes actual)
     */
    @GetMapping("/distribution")
    public ResponseEntity<List<BandwidthBucketReport>> getDistribution(
            @RequestParam(required = false) String period) {

        String resolvedPeriod = period != null ? period : YearMonth.now().toString();
        List<BandwidthBucketReport> report = aggregationService.getDistributionByBucket(resolvedPeriod);
        return ResponseEntity.ok(report);
    }
}
