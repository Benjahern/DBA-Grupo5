package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Repository.RiskZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiskZoneService {

    @Autowired
    private RiskZoneRepository repository;

    public String getAllRiskZonesAsGeoJson() {
        return repository.findAllAsGeoJson();
    }
}
