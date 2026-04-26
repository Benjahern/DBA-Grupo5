package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Entity.Instance;
import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Repository.CpuRepository;
import Host_Usach_Cloud.Backend.Repository.InstanceRepository;
import Host_Usach_Cloud.Backend.Repository.RamRepository;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.DockerClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

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
    public Instance createInstance(String name, Long userId, Long cpuId, Long ramId, Long storageId,
                                          Long regionId, String color, String baseImage){

        //Revisamos los recursos antes
        CPU cpu = cpuRepository.findById(cpuId).orElseThrow(() -> new IllegalArgumentException("El Id de Cpu no existe"));
        Ram ram = ramRepository.findById(ramId).orElseThrow(() -> new IllegalArgumentException("El Id de Ram no existe"));

        CreateContainerResponse container = null;

        /**
         * Esta parte es la complicada, tenemos que traducir nuestros componentes al container de docker
         *
         */
        try{

            //traduccion de quantity a ram de docker (la l es por el numero long)
            long ramDocker = ram.getQuantity() * 1024L * 1024L * 1024L;
            //Ahora lo mismo pero para cpu
            long cpuDocker = cpu.getQuantity() * 1000000000L;

            //configuracion del contenedor
            HostConfig hostConfig = HostConfig.newHostConfig().withMemory(ramDocker).withNanoCPUs(cpuDocker);

            // Recomendado por la ia para identificar
            String containerName = name + "-" + userId + "-" + UUID.randomUUID().toString().substring(0,5);

            //Mandamos el mensaje a docker
            container = dockerClient.createContainerCmd(baseImage).withName(containerName).withHostConfig(hostConfig)
                    .exec();

            //Iniciamos el contenedor
            dockerClient.startContainerCmd(container.getId()).exec();

            String ipAddress = dockerClient.inspectContainerCmd(container.getId()).exec().getNetworkSettings().getIpAddress();

            Instance newInstance = Instance.builder()
                    .Name(name)
                    .Ram_id(ramId)
                    .Cpu_id(cpuId)
                    .Started_at(LocalDateTime.now())
                    .Storage_id(storageId)
                    .Terminated(false)
                    .State("Running")
                    .User_id(userId)
                    .Region_id(regionId)
                    .Container_id(container.getId()) // Guardamos el ID real de Docker
                    .Active_hours(Duration.ZERO)
                    .Ip_address(ipAddress)
                    .Color(color)
                    .build();

            return instanceRepository.save(newInstance);
        } catch ( Exception e){
            if (container != null && container.getId() != null) {
                try {
                    System.err.println("Haciendo rollback en Docker... eliminando contenedor: " + container.getId());
                    dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                } catch (Exception dockerEx) {
                    System.err.println("CRÍTICO: No se pudo eliminar el contenedor huérfano " + container.getId());
                }
            }
            throw new RuntimeException("Error en la instancia en Host Usach Cloud: " + e.getMessage(), e);



        }



    }


    // Solicitado por enunciado
    public InstanceService updateStateByid(Long InstanceId, String State){
        InstanceService

        return instanceUpdates;
    }
}
