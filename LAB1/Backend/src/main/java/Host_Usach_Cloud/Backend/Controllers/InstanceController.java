package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Instance;
import Host_Usach_Cloud.Backend.Services.InstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/instances")
public class InstanceController {

    private final InstanceService instanceService;

    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @PostMapping
    public ResponseEntity<Instance> create(@RequestBody CreateInstanceRequest request) {
        Instance instance = instanceService.createInstance(
                request.getName(),
                request.getUserId(),
                request.getCpuId(),
                request.getRamId(),
                request.getStorageId(),
                request.getRegionId(),
                request.getColor(),
                request.getBaseImage()
        );
        return ResponseEntity.ok(instance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instance> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instanceService.getInstanceById(id));
    }

    @GetMapping
    public ResponseEntity<List<Instance>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String state
    ) {
        List<Instance> instances;

        if (userId != null && state != null) {
            instances = instanceService.getInstancesByUserId(userId)
                    .stream()
                    .filter(instance -> state.equals(instance.getState()))
                    .collect(Collectors.toList());
        } else if (userId != null) {
            instances = instanceService.getInstancesByUserId(userId);
        } else if (state != null) {
            instances = instanceService.getInstancesByState(state);
        } else {
            instances = instanceService.getAllInstances();
        }

        return ResponseEntity.ok(instances);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instance> update(@PathVariable Long id, @RequestBody Instance instance) {
        instance.setInstance_id(id);
        return ResponseEntity.ok(instanceService.updateInstance(instance));
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<Instance> updateState(@PathVariable Long id, @RequestBody UpdateStateRequest request) {
        return ResponseEntity.ok(instanceService.updateStateByid(id, request.getState()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instanceService.deleteInstance(id);
        return ResponseEntity.noContent().build();
    }

    public static class CreateInstanceRequest {
        private String name;
        private Long userId;
        private Long cpuId;
        private Long ramId;
        private Long storageId;
        private Long regionId;
        private String color;
        private String baseImage;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getCpuId() {
            return cpuId;
        }

        public void setCpuId(Long cpuId) {
            this.cpuId = cpuId;
        }

        public Long getRamId() {
            return ramId;
        }

        public void setRamId(Long ramId) {
            this.ramId = ramId;
        }

        public Long getStorageId() {
            return storageId;
        }

        public void setStorageId(Long storageId) {
            this.storageId = storageId;
        }

        public Long getRegionId() {
            return regionId;
        }

        public void setRegionId(Long regionId) {
            this.regionId = regionId;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getBaseImage() {
            return baseImage;
        }

        public void setBaseImage(String baseImage) {
            this.baseImage = baseImage;
        }
    }

    public static class UpdateStateRequest {
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
