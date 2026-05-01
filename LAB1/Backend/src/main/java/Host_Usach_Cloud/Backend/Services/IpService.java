package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Ip;
import Host_Usach_Cloud.Backend.Repository.IpRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IpService {

    private final IpRepository ipRepository;

    public IpService(IpRepository ipRepository) {
        this.ipRepository = ipRepository;
    }

    public Ip create(String ipAddress) {
        Ip ip = Ip.builder()
                .Ip_address(ipAddress)
                .Used(true)
                .build();
        return ipRepository.save(ip);
    }

    public Optional<Ip> getById(Long id) {
        return ipRepository.findById(id);
    }

    public Optional<Ip> findByAddress(String address) {
        return ipRepository.findByAddress(address);
    }

    public boolean toggleUsed(Long id) {
        Optional<Ip> ipOpt = ipRepository.findById(id);
        if (ipOpt.isEmpty()) {
            throw new IllegalArgumentException("IP no encontrada con id: " + id);
        }
        Ip ip = ipOpt.get();
        return ipRepository.updateUsed(id, !ip.isUsed());
    }
}