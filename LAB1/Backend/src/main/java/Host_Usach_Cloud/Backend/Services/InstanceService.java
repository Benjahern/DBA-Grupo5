package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Repository.CpuRepository;
import Host_Usach_Cloud.Backend.Repository.InstanceRepository;
import Host_Usach_Cloud.Backend.Repository.RamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.DockerClient;

@Service
public class InstanceService {

    private final DockerClient dockerClient;
    private final CpuRepository cpuRepository;
    private final RamRepository ramRepository;
    private final InstanceRepository instanceRepository;

    public InstanceService(DockerClient dockerClient, CpuRepository cpuRepository,
                           RamRepository ramRepository, InstanceRepository instanceRepository) {
        this.dockerClient = dockerClient;
        this.cpuRepository = cpuRepository;
        this.ramRepository = ramRepository;
        this.instanceRepository = instanceRepository;
    }

    //En proceso, me faltan los demas repos (benja h)
    public InstanceService createInstance(String name, Long userId, Long cpuId, Long ramId, Long storageId,
                                          Long regionId, String color, String baseImage){

    }


    // Solicitado por enunciado
    public InstanceService updateStateByid(Long InstanceId, String State){
        InstanceService

        return instanceUpdates;
    }
}
