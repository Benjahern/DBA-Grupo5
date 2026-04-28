package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Services.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
