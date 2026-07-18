package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Datacenter;
import Host_Usach_Cloud.Backend.Services.DTO.LocationRequest;
import Host_Usach_Cloud.Backend.Services.DTO.LocationResponse;
import Host_Usach_Cloud.Backend.Services.DatacenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/datacenters")
@RequiredArgsConstructor
public class DatacenterController {

    private final DatacenterService datacenterService;

    @PostMapping
    public ResponseEntity<Long> createDatacenter(
            @RequestBody Datacenter datacenter
    ) {
        Long id =
                datacenterService.createDatacenter(
                        datacenter
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(id);
    }

    @GetMapping
    public ResponseEntity<List<Datacenter>> getAllDatacenters() {
        return ResponseEntity.ok(
                datacenterService.getAllDatacenters()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Datacenter> getDatacenterById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                datacenterService.getDatacenterById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDatacenter(
            @PathVariable Long id,
            @RequestBody Datacenter datacenter
    ) {
        datacenterService.updateDatacenter(
                id,
                datacenter
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatacenter(
            @PathVariable Long id
    ) {
        datacenterService.deleteDatacenter(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/location-info")
    public ResponseEntity<LocationResponse> getLocationInfo(
            @RequestBody LocationRequest request
    ) {
        return ResponseEntity.ok(
                datacenterService.getLocationInfo(
                        request.getLatitude(),
                        request.getLongitude()
                )
        );
    }
}