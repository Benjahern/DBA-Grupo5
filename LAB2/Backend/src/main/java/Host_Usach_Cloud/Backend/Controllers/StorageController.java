package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Storage;
import Host_Usach_Cloud.Backend.Services.StorageService;
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
@RequestMapping("/api/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<Storage> create(@RequestBody Storage storage) {
        return ResponseEntity.ok(storageService.createStorage(storage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Storage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getStorageById(id));
    }

    @GetMapping
    public ResponseEntity<List<Storage>> getAll() {
        return ResponseEntity.ok(storageService.getAllStorage());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Storage> update(@PathVariable Long id, @RequestBody Storage storage) {
        storage.setStorage_id(id);
        return ResponseEntity.ok(storageService.updateStorage(storage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storageService.deleteStorage(id);
        return ResponseEntity.noContent().build();
    }
}
