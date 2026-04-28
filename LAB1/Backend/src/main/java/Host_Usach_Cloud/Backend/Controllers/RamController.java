package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Services.RamService;
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
@RequestMapping("/api/rams")
public class RamController {

    private final RamService ramService;

    public RamController(RamService ramService) {
        this.ramService = ramService;
    }

    @PostMapping
    public ResponseEntity<Ram> create(@RequestBody Ram ram) {
        return ResponseEntity.ok(ramService.createRam(ram));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ram> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ramService.getRamById(id));
    }

    @GetMapping
    public ResponseEntity<List<Ram>> getAll() {
        return ResponseEntity.ok(ramService.getAllRam());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ram> update(@PathVariable Long id, @RequestBody Ram ram) {
        ram.setRam_id(id);
        return ResponseEntity.ok(ramService.updateRam(ram));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ramService.deleteRam(id);
        return ResponseEntity.noContent().build();
    }
}
