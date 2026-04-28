package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Repository.RamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RamService {

    private final RamRepository ramRepository;

    public RamService(RamRepository ramRepository) {
        this.ramRepository = ramRepository;
    }

    public Ram createRam(Ram ram) {
        return ramRepository.save(ram);
    }

    public Ram getRamById(Long ramId) {
        return ramRepository.findById(ramId)
                .orElseThrow(() -> new IllegalArgumentException("El Id de Ram no existe"));
    }

    public List<Ram> getAllRam() {
        return ramRepository.findAll();
    }

    public Ram updateRam(Ram ram) {
        boolean updated = ramRepository.update(ram);
        if (!updated) {
            throw new IllegalArgumentException("El Id de Ram no existe");
        }
        return ram;
    }

    public void deleteRam(Long ramId) {
        boolean deleted = ramRepository.deleteById(ramId);
        if (!deleted) {
            throw new IllegalArgumentException("El Id de Ram no existe");
        }
    }
}
