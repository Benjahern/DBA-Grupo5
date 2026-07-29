package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Services.ConsumptionService;
import Host_Usach_Cloud.Backend.Services.DTO.ConsumptionProjection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @GetMapping("/users/{userId}/monthly-projection")
    public ResponseEntity<ConsumptionProjection> getMonthlyProjection(@PathVariable Long userId) {
        return ResponseEntity.ok(consumptionService.getProjectedMonthlyConsumptionByUser(userId));
    }
}
