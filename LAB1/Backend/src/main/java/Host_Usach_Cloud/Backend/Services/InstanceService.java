package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Instance;
import Host_Usach_Cloud.Backend.Repository.CpuRepository;
import Host_Usach_Cloud.Backend.Repository.InstanceRepository;
import Host_Usach_Cloud.Backend.Repository.RamRepository;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.DockerClient;

import java.sql.CallableStatement;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Instance createInstance(String name, Long userId, Long cpuId, Long ramId, Long storageId,
                                   Long regionId, String color, String baseImage) {

        System.out.println("Enviando usuario: " + userId);
        // 1. OBTENER RECURSOS (Corregido con comillas y nombres exactos)
        Map<String, Object> ramData = jdbcTemplate.queryForMap(
                "SELECT \"Quantity\" FROM \"Ram\" WHERE \"Ram_id\" = ?", ramId);
        Map<String, Object> cpuData = jdbcTemplate.queryForMap(
                "SELECT \"Quantity\" FROM \"CPU\" WHERE \"Cpu_id\" = ?", cpuId);

        long ramQuantity = ((Number) ramData.get("Quantity")).longValue();
        long cpuQuantity = ((Number) cpuData.get("Quantity")).longValue();

        // 2. LLAMADA MANUAL AL PROCEDIMIENTO (Sin fallos de metadatos)
        String sql = "CALL provision_instance_sp(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<String, Object> out = jdbcTemplate.execute(sql, (CallableStatement cs) -> {
            cs.setString(1, name);
            cs.setLong(2, userId);
            cs.setLong(3, cpuId);
            cs.setLong(4, ramId);
            cs.setLong(5, storageId);
            cs.setLong(6, regionId);
            cs.setString(7, color);
            cs.setString(8, baseImage);

            // Registrar parámetros OUT (9 y 10)
            cs.registerOutParameter(9, Types.BIGINT);   // p_new_instance_id
            cs.registerOutParameter(10, Types.VARCHAR); // p_assigned_ip

            cs.execute();

            Map<String, Object> res = new HashMap<>();
            res.put("id", cs.getLong(9));
            res.put("ip", cs.getString(10));
            return res;
        });

        Long newId = (Long) out.get("id");
        String assignedIp = (String) out.get("ip");

        try {
            // 3. LÓGICA DE DOCKER (Se mantiene igual)
            long ramDocker = ramQuantity * 1024L * 1024L * 1024L;
            long cpuDocker = cpuQuantity * 1000000000L;

            String dockerName = name.replaceAll("\\s+", "-") + "-" + newId;

            CreateContainerResponse container = dockerClient.createContainerCmd(baseImage)
                    .withName(dockerName)
                    .withHostConfig(HostConfig.newHostConfig().withMemory(ramDocker).withNanoCPUs(cpuDocker))
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();

            // 4. ACTUALIZACIÓN FINAL EN BD
            jdbcTemplate.update(
                    "UPDATE \"Instance\" SET \"Container_id\" = ?, \"State\" = 'Running' WHERE \"Instance_id\" = ?",
                    container.getId(), newId);

            // 5. RETORNAR EL OBJETO COMPLETO
            return Instance.builder()
                    .Instance_id(newId)         // De la base de datos
                    .Name(name)                // Del parámetro de entrada
                    .Ram_id(ramId)             // Del parámetro de entrada
                    .Cpu_id(cpuId)             // Del parámetro de entrada
                    .Storage_id(storageId)     // Del parámetro de entrada
                    .Region_id(regionId)       // Del parámetro de entrada
                    .User_id(userId)           // Del parámetro de entrada
                    .Color(color)              // Del parámetro de entrada
                    .Base_image(baseImage)     // Del parámetro de entrada
                    .Ip_address(assignedIp)    // De la base de datos (SP)
                    .Container_id(container.getId()) // De Docker
                    .State("Running")          // Estado final
                    .Terminated(false)         // Valor por defecto
                    .Started_at(LocalDateTime.now()) // Fecha actual
                    .Active_hours(Duration.ZERO)     // Inicializado
                    .build();

        } catch (Exception e) {
            handleProvisioningFailure(newId, assignedIp);
            throw new RuntimeException("Fallo en Docker: " + e.getMessage());
        }
    }

    private void handleProvisioningFailure(Long instanceId, String ipAddress) {
        try {
            // Liberar IP en tabla "Ip"
            jdbcTemplate.update("UPDATE \"Ip\" SET \"Used\" = FALSE WHERE \"Ip_address\" = ?", ipAddress);

            // Eliminar Ticket y luego Instancia
            jdbcTemplate.update("DELETE FROM \"Ticket\" WHERE \"Instance_id\" = ?", instanceId);
            jdbcTemplate.update("DELETE FROM \"Instance\" WHERE \"Instance_id\" = ?", instanceId);
        } catch (Exception e) {
            System.err.println("Error en limpieza: " + e.getMessage());
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
            dockerClient.stopContainerCmd(instance.getContainer_id()).exec();
            dockerClient.removeContainerCmd(instance.getContainer_id()).withForce(true).exec();

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
}
