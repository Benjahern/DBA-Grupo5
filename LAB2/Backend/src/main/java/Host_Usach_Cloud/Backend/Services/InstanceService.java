package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Entity.Instance;
import Host_Usach_Cloud.Backend.Entity.Ip;
import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Repository.InstanceRepository;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.DockerClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InstanceService {

    private final DockerClient dockerClient;
    private final CpuService cpuService;
    private final RamService ramService;
    private final IpService ipService;
    private final InstanceRepository instanceRepository;

    public InstanceService(DockerClient dockerClient, CpuService cpuService,
                           RamService ramService, IpService ipService,
                           InstanceRepository instanceRepository) {
        this.dockerClient = dockerClient;
        this.cpuService = cpuService;
        this.ramService = ramService;
        this.ipService = ipService;
        this.instanceRepository = instanceRepository;
    }

    //En proceso, me faltan los demas repos (benja h)
    public Instance createInstance(String name, Long userId, Long cpuId, Long ramId, Long storageId,
                                          Long regionId, Long datacenterId, String color, String baseImage){

        //Revisamos los recursos antes
        CPU cpu = cpuService.getCpuById(cpuId);
        Ram ram = ramService.getRamById(ramId);

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

            // Asegurar que la imagen existe (pull si no existe)
            ensureImageExists(baseImage);

            // Mandamos el mensaje a docker
            // IMPORTANTE: Un contenedor ubuntu:latest por defecto corre un shell y sale inmediatamente si no se le pasa un comando continuo
            // Usaremos "tail -f /dev/null" o "sleep infinity" para que se quede corriendo
            container = dockerClient.createContainerCmd(baseImage)
                    .withName(containerName)
                    .withHostConfig(hostConfig)
                    .withCmd("tail", "-f", "/dev/null")
                    .exec();

            //Iniciamos el contenedor
            dockerClient.startContainerCmd(container.getId()).exec();

            String ipAddress = resolveContainerIp(container.getId());

            // Asegurar que la IP existe en la BD (crear si no existe)
            ipService.findByAddress(ipAddress)
                    .orElseGet(() -> ipService.create(ipAddress));

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
                    .Datacenter_id(datacenterId)
                    .Container_id(container.getId())
                    .Active_hours(Duration.ZERO)
                    .Ip_address(ipAddress)
                    .Color(color)
                    .build();

            // Crear Instance primero para obtener el Instance_id
            instanceRepository.save(newInstance);

            // Llamar stored procedure para asignar IP y crear Ticket
            instanceRepository.provisionInstance(ipAddress, newInstance.getInstance_id());

            return newInstance;
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

    public Instance getInstanceById(Long instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("El Id de la instancia no existe"));
    }

    public List<Instance> getAllInstances() {
        return instanceRepository.findAll();
    }

    public List<Instance> getInstancesByUserId(Long userId) {
        return instanceRepository.findAllByUserId(userId);
    }

    public List<Instance> getInstancesByState(String state) {
        return instanceRepository.findAllByState(state);
    }

    public Instance updateInstance(Instance instance) {
        boolean updated = instanceRepository.update(instance);
        if (!updated) {
            throw new IllegalArgumentException("El Id de la instancia no existe");
        }
        return instance;
    }

    public void deleteInstance(Long instanceId) {
        boolean deleted = instanceRepository.deleteById(instanceId);
        if (!deleted) {
            throw new IllegalArgumentException("El Id de la instancia no existe");
        }
    }

    public Flux<Statistics> getContainerStatsReactive(String containerId) {
        return Flux.create(sink -> {
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> callback = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics stats) {
                    sink.next(stats);
                }
            };
            statsCmd.exec(callback);
            sink.onCancel(() -> {
                try {
                    callback.close();
                    statsCmd.close();
                } catch (Exception ignored) {}
            });
        });
    }

    // Solicitado por enunciado
    public Instance updateStateByid(Long InstanceId, String State) {

        Instance instance = instanceRepository.findById(InstanceId)
            .orElseThrow(() -> new IllegalArgumentException("El Id de la instancia no existe"));

        if (State.equals("Stopped") && instance.getState().equals("Running")) {
            // Parar el contenedor en Docker
            dockerClient.stopContainerCmd(instance.getContainer_id()).exec();

            // Actualizar el estado. 
            // El trigger sumará el tiempo a Active_hours y pondrá Started_at = null
            instance.setState("Stopped");

        } else if (State.equals("Running") && instance.getState().equals("Stopped")) {
            // Iniciar el contenedor en Docker
            dockerClient.startContainerCmd(instance.getContainer_id()).exec();

            // Actualizar el estado. 
            // El trigger pondrá automáticamente Started_at = NOW()
            instance.setState("Running");

        } else if (State.equals("Terminated")) {
            // Terminar el contenedor en Docker y eliminarlo
            try {
                dockerClient.stopContainerCmd(instance.getContainer_id()).exec();
            } catch (Exception e) {
                // If it is already stopped, docker normally throws NotModifiedException.
                System.out.println("Contenedor ya estaba detenido o no se pudo detener: " + e.getMessage());
            }
            try {
                dockerClient.removeContainerCmd(instance.getContainer_id()).withForce(true).exec();
            } catch (Exception e) {
                // If container doesn't exist (already deleted), continue
                System.out.println("Contenedor no existe o ya fue eliminado: " + e.getMessage());
            }

            // Actualizar el estado y marcar como terminado.
            // El trigger liberará la IP, sumará el tiempo final a Active_hours y limpiará Started_at
            instance.setState("Terminated");
            instance.setTerminated(true);

        } else {
            throw new IllegalArgumentException("Estado no válido o transición no permitida");
        }

  
        instanceRepository.update(instance);

        return instance;
    }

    private String resolveContainerIp(String containerId) {
        for (int attempt = 0; attempt < 5; attempt++) {
            var inspect = dockerClient.inspectContainerCmd(containerId).exec();
            if (inspect.getNetworkSettings() != null && inspect.getNetworkSettings().getNetworks() != null) {
                for (Map.Entry<String, com.github.dockerjava.api.model.ContainerNetwork> entry
                        : inspect.getNetworkSettings().getNetworks().entrySet()) {
                    String ip = entry.getValue().getIpAddress();
                    if (ip != null && !ip.isBlank()) {
                        return ip;
                    }
                }
            }
            String legacyIp = inspect.getNetworkSettings() != null ? inspect.getNetworkSettings().getIpAddress() : null;
            if (legacyIp != null && !legacyIp.isBlank()) {
                return legacyIp;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null;
    }

    private void ensureImageExists(String imageName) {
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (Exception e) {
            System.out.println("Imagen no encontrada, haciendo pull: " + imageName);
            dockerClient.pullImageCmd(imageName).start();
        }
    }
}
