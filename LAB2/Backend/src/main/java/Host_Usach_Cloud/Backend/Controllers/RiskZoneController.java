package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Services.RiskZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risks")
public class RiskZoneController {

    @Autowired
    private RiskZoneService riskZoneService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<String> getRiskZones() {
        String geoJson = riskZoneService.getAllRiskZonesAsGeoJson();
        return ResponseEntity.ok(geoJson);
    }
}