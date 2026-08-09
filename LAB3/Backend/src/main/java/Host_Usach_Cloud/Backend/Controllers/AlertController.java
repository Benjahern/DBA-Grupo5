package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import Host_Usach_Cloud.Backend.Mongo.Services.AlertService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // El frontend debe conectarse a esta ruta pasando su userId
    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AlertDocument> streamUserAlerts(@PathVariable Long userId) {
        return alertService.getAlertStreamForUser(userId);
    }
}
