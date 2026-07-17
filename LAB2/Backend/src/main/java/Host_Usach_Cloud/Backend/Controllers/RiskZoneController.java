package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Services.RiskZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk-zone")
public class RiskZoneController {

    @Autowired
    private RiskZoneService riskZoneService; // Inyectamos el servicio con el nombre actualizado

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllZonas() {
        return ResponseEntity.ok(riskZoneService.obtenerTodasLasZonasGeoJson());
    }

    @GetMapping(value = "/type/{tipo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getZonasByType(@PathVariable String tipo) {
        return ResponseEntity.ok(riskZoneService.obtenerZonasPorTipoGeoJson(tipo));
    }
}