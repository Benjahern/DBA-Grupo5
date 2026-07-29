package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Region;
import Host_Usach_Cloud.Backend.Services.DTO.PingResult;
import Host_Usach_Cloud.Backend.Services.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {
    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping
    public ResponseEntity<Region> create(@RequestBody Region Region) {
        return ResponseEntity.ok(regionService.createRegion(Region));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Region> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getRegionById(id));
    }

    @GetMapping
    public ResponseEntity<List<Region>> getAll() {
        return ResponseEntity.ok(regionService.getAllRegions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Region> update(@PathVariable Long id, @RequestBody Region Region) {
        Region.setRegion_id(id);
        return ResponseEntity.ok(regionService.updateRegion(Region));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public ResponseEntity<List<PingResult>> ping(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        if (lat == null || lng == null
                || lat < -90 || lat > 90
                || lng < -180 || lng > 180) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(regionService.getLatencyToRegions(lat, lng));
    }
}
