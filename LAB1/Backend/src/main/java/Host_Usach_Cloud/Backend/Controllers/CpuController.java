package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Services.CpuService;
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
@RequestMapping("/api/cpus")
public class CpuController {

    private final CpuService cpuService;

    public CpuController(CpuService cpuService) {
        this.cpuService = cpuService;
    }

    @PostMapping
    public ResponseEntity<CPU> create(@RequestBody CPU cpu) {
        return ResponseEntity.ok(cpuService.createCpu(cpu));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CPU> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cpuService.getCpuById(id));
    }

    @GetMapping
    public ResponseEntity<List<CPU>> getAll() {
        return ResponseEntity.ok(cpuService.getAllCpu());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CPU> update(@PathVariable Long id, @RequestBody CPU cpu) {
        cpu.setCpu_id(id);
        return ResponseEntity.ok(cpuService.updateCpu(cpu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cpuService.deleteCpu(id);
        return ResponseEntity.noContent().build();
    }
}
