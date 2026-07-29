package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Region;
import Host_Usach_Cloud.Backend.Repository.RegionRepository;
import Host_Usach_Cloud.Backend.Services.DTO.PingResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public Region createRegion(Region region){
        return regionRepository.save(region);
    }

    public Region getRegionById(Long regionId){
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("El Id de Region no existe"));
    }

    public List<Region> getAllRegions(){
        return regionRepository.findAll();
    }

    public Region updateRegion(Region region){
        boolean updated = regionRepository.update(region);
        if (!updated) {
            throw new IllegalArgumentException("El Id de Region no existe");
        }
        return region;
    }

    public void deleteRegion(Long regionId){
        boolean deleted = regionRepository.deleteById(regionId);
        if (!deleted) {
            throw new IllegalArgumentException("El Id de Region no existe");
        }
    }

    public List<PingResult> getLatencyToRegions(double lat, double lng) {
        return regionRepository.getLatencyToRegions(lat, lng);
    }
}
