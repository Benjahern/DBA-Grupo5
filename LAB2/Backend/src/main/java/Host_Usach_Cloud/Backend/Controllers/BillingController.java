package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Services.DTO.BillingDistanceRequest;
import Host_Usach_Cloud.Backend.Services.DTO.BillingDistanceResult;
import Host_Usach_Cloud.Backend.Services.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/users/{userId}/monthly-tickets")
    public ResponseEntity<Void> generateMonthlyTickets(@PathVariable Long userId) {
        billingService.generateMonthlyTickets(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/instances/{instanceId}/calculate-distance")
    public ResponseEntity<BillingDistanceResult> calculateDistance(
            @PathVariable Long instanceId,
            @RequestBody BillingDistanceRequest request) {
        BillingDistanceResult result = billingService.calculateDistanceBilling(
                instanceId, request.userLat(), request.userLon());
        return ResponseEntity.ok(result);
    }
}
