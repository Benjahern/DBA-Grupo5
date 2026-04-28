package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Repository.CpuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CpuService {

    private final CpuRepository cpuRepository;

    public CpuService(CpuRepository cpuRepository) {
        this.cpuRepository = cpuRepository;
    }

    public CPU createCpu(CPU cpu) {
        return cpuRepository.save(cpu);
    }

    public CPU getCpuById(Long cpuId) {
        return cpuRepository.findById(cpuId)
                .orElseThrow(() -> new IllegalArgumentException("El Id de Cpu no existe"));
    }

    public List<CPU> getAllCpu() {
        return cpuRepository.findAll();
    }

    public CPU updateCpu(CPU cpu) {
        boolean updated = cpuRepository.update(cpu);
        if (!updated) {
            throw new IllegalArgumentException("El Id de Cpu no existe");
        }
        return cpu;
    }

    public void deleteCpu(Long cpuId) {
        boolean deleted = cpuRepository.deleteById(cpuId);
        if (!deleted) {
            throw new IllegalArgumentException("El Id de Cpu no existe");
        }
    }
}
