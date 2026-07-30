package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.CPU;
import Host_Usach_Cloud.Backend.Entity.Ip;
import Host_Usach_Cloud.Backend.Entity.Ram;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import Host_Usach_Cloud.Backend.Mongo.Exceptions.InstanceNotFoundException;
import Host_Usach_Cloud.Backend.Mongo.Exceptions.QuotaExceededException;
import Host_Usach_Cloud.Backend.Mongo.Services.QuotaService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de instancias. La entidad Instance vive ahora en MongoDB.
 * Postgres conserva: catálogo (CPU/RAM/Storage/Region/Datacenter), pool de IPs,
 * tickets y métricas (todavía). La cuota (Req 2) la enforce el patrón de
 * contador en client_quotas; el resto de la lógica que estaba en triggers
 * (release de IP, active_hours, distance check) se porta a este servicio.
 *
 * <p>Los IDs de instancia son <b>numéricos sequential per-user</b> en el campo
 * {@code numericId} (1, 2, 3...). El ObjectId hex en {@code instanceId} se conserva
 * como PK interna de Mongo (para FK Postgres) pero ya no se expone al usuario.</p>
 */
@Service
public class InstanceService {

    private final DockerClient dockerClient;
    private final CpuService cpuService;
    private final RamService ramService;
    private final IpService ipService;
    private final QuotaService quotaService;
    private final MongoTemplate mongoTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate mongoTxTemplate;

    public InstanceService(DockerClient dockerClient,
                           CpuService cpuService,
                           RamService ramService,
                           IpService ipService,
                           QuotaService quotaService,
                           MongoTemplate mongoTemplate,
                           JdbcTemplate jdbcTemplate,
                           org.springframework.data.mongodb.MongoTransactionManager mongoTxManager) {
        this.dockerClient = dockerClient;
        this.cpuService = cpuService;
        this.ramService = ramService;
        this.ipService = ipService;
        this.quotaService = quotaService;
        this.mongoTemplate = mongoTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.mongoTxTemplate = new TransactionTemplate(mongoTxManager);
    }

    public InstanceDocument createInstance(String name, Long userId, Long cpuId, Long ramId, Long storageId,
                                           Long regionId, Long datacenterId, String color, String baseImage) {

        // (A) Validaciones de catálogo contra Postgres
        CPU cpu = cpuService.getCpuById(cpuId);
        Ram ram = ramService.getRamById(ramId);
        if (cpu == null) throw new IllegalArgumentException("CPU no existe: " + cpuId);
        if (ram == null) throw new IllegalArgumentException("RAM no existe: " + ramId);

        // (B) Distance check (PostGIS, ≤ 4300 km) — port del trigger
        validateDistance(regionId, datacenterId);

        // Generamos el ID antes para poder compensar si algo falla
        String objectIdHex = new ObjectId().toHexString();

        // (C) numericId per-user: max(existente del user) + 1. Coincide con
        // BIGSERIAL pre-migración pero particionado por userId, así cada
        // usuario ve su primera instancia como id=1.
        Long nextNumericId = mongoTemplate.find(
                Query.query(Criteria.where("userId").is(userId))
                        .with(Sort.by(Sort.Direction.DESC, "numericId"))
                        .limit(1),
                InstanceDocument.class, "instances"
        ).stream()
                .map(InstanceDocument::getNumericId)
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(0L) + 1;

        // (D) Transacción Mongo CORTA: quota + insert instance
        InstanceDocument doc = InstanceDocument.builder()
                .instanceId(objectIdHex)
                .numericId(nextNumericId)
                .name(name)
                .state("Running")          // arranca Running; si Docker falla, compensate() lo borra
                .userId(userId)
                .cpuId(cpuId)
                .ramId(ramId)
                .storageId(storageId)
                .regionId(regionId)
                .datacenterId(datacenterId)
                .containerId(null)
                .startedAt(LocalDateTime.now())
                .activeHours(0.0)
                .terminated(false)
                .ipAddress(null)
                .color(color)
                .build();

        try {
            mongoTxTemplate.execute(status -> {
                quotaService.reserve(userId);
                mongoTemplate.insert(doc, "instances");
                return null;
            });
        } catch (QuotaExceededException qe) {
            throw qe;
        } catch (Exception e) {
            throw new RuntimeException("Error reservando cuota/insertando instancia: " + e.getMessage(), e);
        }

        // (E) Docker provisioning + reserva de IP en Postgres (FUERA de la tx Mongo)
        CreateContainerResponse container = null;
        String containerId = null;
        String ipAddress = null;
        try {
            long ramDocker = ram.getQuantity() * 1024L * 1024L * 1024L;
            long cpuDocker = cpu.getQuantity() * 1000000000L;
            HostConfig hostConfig = HostConfig.newHostConfig().withMemory(ramDocker).withNanoCPUs(cpuDocker);

            String containerName = name + "-" + userId + "-" + UUID.randomUUID().toString().substring(0, 5);
            ensureImageExists(baseImage);

            container = dockerClient.createContainerCmd(baseImage)
                    .withName(containerName)
                    .withHostConfig(hostConfig)
                    .withCmd("tail", "-f", "/dev/null")
                    .exec();
            containerId = container.getId();

            dockerClient.startContainerCmd(containerId).exec();
            final String resolvedIp = resolveContainerIp(containerId);
            ipAddress = resolvedIp;

            // Asegurar IP en BD y marcarla Assigned
            Ip ip = ipService.findByAddress(resolvedIp)
                    .orElseGet(() -> ipService.create(resolvedIp));
            if (!ip.isAssigned()) {
                jdbcTemplate.update("UPDATE \"Ip\" SET \"Assigned\" = TRUE WHERE \"Ip_id\" = ?", ip.getIp_id());
            }

            // Crear Ticket inicial con FK al numericId (Postgres.Instance_id ahora es BIGINT)
            jdbcTemplate.update(
                    "INSERT INTO \"Ticket\" (\"Status\", \"Description\", \"Instance_id\", \"User_id\") " +
                            "VALUES ('Open', ?, ?, ?)",
                    "Instancia provisionada",
                    nextNumericId,
                    userId);

            // Actualizar doc Mongo con containerId, ipAddress y dejar state Running
            // Usamos los nombres BSON directamente (State, Started_at) para no
            // depender de QueryMapper translate de Update (que varía entre versiones
            // de Spring Data). Criteria sí se traduce al pasar InstanceDocument.class.
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(objectIdHex)),
                    new Update()
                            .set("containerId", containerId)
                            .set("ipAddress", ipAddress)
                            .set("State", "Running")
                            .set("Started_at", LocalDateTime.now()),
                    InstanceDocument.class, "instances");

            return findByNumericId(nextNumericId);

        } catch (Exception e) {
            // Compensación: rollback Docker + liberar IP + borrar doc Mongo + liberar cuota
            System.err.println("Fallo en provisioning — ejecutando compensación: " + e.getMessage());
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception dex) {
                    System.err.println("CRÍTICO: no se pudo eliminar contenedor huérfano " + containerId);
                }
            }
            if (ipAddress != null) {
                try {
                    jdbcTemplate.update(
                            "UPDATE \"Ip\" SET \"Assigned\" = FALSE WHERE \"Ip_address\" = ? AND \"Assigned\" = TRUE",
                            ipAddress);
                } catch (Exception ignored) {}
            }
            try {
                mongoTxTemplate.execute(status -> {
                    mongoTemplate.remove(
                            Query.query(Criteria.where("_id").is(objectIdHex)),
                            "instances");
                    quotaService.release(userId);
                    return null;
                });
            } catch (Exception ce) {
                System.err.println("CRÍTICO: compensación Mongo falló: " + ce.getMessage());
            }
            throw new RuntimeException("Error en la instancia en Host Usach Cloud: " + e.getMessage(), e);
        }
    }

    private void validateDistance(Long regionId, Long datacenterId) {
        if (datacenterId == null) return;
        Double km = jdbcTemplate.queryForObject(
                "SELECT ST_Distance(" +
                        "  ST_SetSRID(ST_MakePoint(d.longitude, d.latitude), 4326)::geography," +
                        "  ST_Centroid(r.\"Geom\")::geography" +
                        ") / 1000.0 " +
                        "FROM \"Datacenter\" d, \"Region\" r " +
                        "WHERE d.id = ? AND r.\"Region_id\" = ?",
                Double.class, datacenterId, regionId);
        if (km == null) {
            throw new IllegalArgumentException("No se pudo calcular la distancia datacenter↔region");
        }
        if (km > 4300.0) {
            throw new IllegalArgumentException(
                    "Soberanía de datos: datacenter a " + Math.round(km) + " km de la región (>4300 km)");
        }
    }

    public InstanceDocument getInstanceById(Long numericId) {
        return findByNumericId(numericId);
    }

    public List<InstanceDocument> getAllInstances() {
        return mongoTemplate.findAll(InstanceDocument.class, "instances");
    }

    public List<InstanceDocument> getInstancesByUserId(Long userId) {
        return mongoTemplate.find(
                Query.query(Criteria.where("userId").is(userId)),
                InstanceDocument.class, "instances");
    }

    public List<InstanceDocument> getInstancesByState(String state) {
        return mongoTemplate.find(
                Query.query(Criteria.where("state").is(state)),
                InstanceDocument.class, "instances");
    }
    // Nota: pasar InstanceDocument.class es necesario para que QueryMapper aplique
    // el @Field("State") y consulte el campo BSON renombrado.

    public InstanceDocument updateInstance(InstanceDocument instance) {
        mongoTemplate.save(instance, "instances");
        return instance;
    }

    public void deleteInstance(Long numericId) {
        InstanceDocument inst = findByNumericId(numericId);
        if ("Running".equals(inst.getState()) || "Stopped".equals(inst.getState())) {
            throw new IllegalArgumentException("Termina la instancia antes de borrarla");
        }
        try {
            containerCleanup(inst);
        } catch (Exception ignored) {}
        mongoTxTemplate.execute(status -> {
            mongoTemplate.remove(Query.query(Criteria.where("numericId").is(numericId)), "instances");
            quotaService.release(inst.getUserId());
            return null;
        });
    }

    public InstanceDocument updateStateByid(Long numericId, String newState) {
        InstanceDocument inst = findByNumericId(numericId);
        String oldState = inst.getState();
        if (newState.equals(oldState)) return inst;

        // (1) Acción Docker primero
        if ("Stopped".equals(newState) && "Running".equals(oldState)) {
            dockerClient.stopContainerCmd(inst.getContainerId()).exec();
        } else if ("Running".equals(newState) && "Stopped".equals(oldState)) {
            dockerClient.startContainerCmd(inst.getContainerId()).exec();
        } else if ("Terminated".equals(newState)) {
            containerCleanup(inst);
        } else {
            throw new IllegalArgumentException("Transición no permitida: " + oldState + " -> " + newState);
        }

        // (2) Aggregation update pipeline — transición atómica en Mongo.
        // Acumula Active_hours cuando oldState=Running y newState ∈ {Stopped, Terminated},
        // refresca Started_at cuando newState=Running desde Stopped/Terminated, lo
        // resetea a null cuando Running → Stopped/Terminated. Limpia terminated e
        // ipAddress en Terminated. Todas las expresiones evalúan contra el documento
        // de entrada (mismo $set stage), no contra el parcialmente modificado.
        List<String> stopOrTerminated = Arrays.asList("Stopped", "Terminated");

        Document activeHoursExpr = new Document("$cond", new Document()
                .append("if", new Document("$and", Arrays.asList(
                        new Document("$eq", Arrays.asList("$State", "Running")),
                        new Document("$in", Arrays.asList(newState, stopOrTerminated)),
                        new Document("$ne", Arrays.asList("$Started_at", null)))))
                .append("then", new Document("$add", Arrays.asList(
                        new Document("$ifNull", Arrays.asList("$Active_hours", 0.0)),
                        new Document("$divide", Arrays.asList(
                                new Document("$subtract", Arrays.asList("$$NOW", "$Started_at")),
                                3600000.0)))))
                .append("else", new Document("$ifNull", Arrays.asList("$Active_hours", 0.0))));

        Document startedAtExpr = new Document("$switch", new Document()
                .append("branches", Arrays.asList(
                        new Document("case", new Document("$and", Arrays.asList(
                                new Document("$in", Arrays.asList("$State", stopOrTerminated)),
                                new Document("$eq", Arrays.asList(newState, "Running")))))
                                .append("then", "$$NOW"),
                        new Document("case", new Document("$and", Arrays.asList(
                                new Document("$eq", Arrays.asList("$State", "Running")),
                                new Document("$in", Arrays.asList(newState, stopOrTerminated)))))
                                .append("then", null)))
                .append("default", "$Started_at"));

        Document setStage = new Document()
                .append("Active_hours", activeHoursExpr)
                .append("Started_at", startedAtExpr)
                .append("State", newState);

        if ("Terminated".equals(newState)) {
            setStage.append("terminated", true).append("ipAddress", null);
        }

        AggregationUpdate pipeline = AggregationUpdate.from(
                List.<AggregationOperation>of(ctx -> new Document("$set", setStage)));

        // Compare-and-set sobre el estado anterior (Criteria.where("state") se mapea
        // a {State: oldState} vía QueryMapper gracias a InstanceDocument.class).
        Query cas = Query.query(Criteria.where("numericId").is(numericId).and("state").is(oldState));

        // (3) Si termina, liberar IP en Postgres y decrementar cuota (en tx Mongo)
        if ("Terminated".equals(newState)) {
            final String oldIp = inst.getIpAddress();
            if (oldIp != null) {
                try {
                    jdbcTemplate.update(
                            "UPDATE \"Ip\" SET \"Assigned\" = FALSE WHERE \"Ip_address\" = ? AND \"Assigned\" = TRUE",
                            oldIp);
                } catch (Exception e) {
                    System.err.println("Aviso: no se pudo liberar IP " + oldIp + ": " + e.getMessage());
                }
            }
            final Long userId = inst.getUserId();
            UpdateResult res = mongoTxTemplate.execute(status -> {
                UpdateResult r = mongoTemplate.updateFirst(cas, pipeline, InstanceDocument.class, "instances");
                quotaService.release(userId);
                return r;
            });
            if (res == null || res.getMatchedCount() == 0) {
                throw new IllegalStateException("Transición concurrente sobre instancia " + numericId);
            }
        } else {
            UpdateResult res = mongoTemplate.updateFirst(cas, pipeline, InstanceDocument.class, "instances");
            if (res.getMatchedCount() == 0) {
                throw new IllegalStateException("Transición concurrente sobre instancia " + numericId);
            }
        }

        return findByNumericId(numericId);
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
                try { callback.close(); statsCmd.close(); } catch (Exception ignored) {}
            });
        });
    }

    private InstanceDocument findByNumericId(Long numericId) {
        InstanceDocument inst = mongoTemplate.findOne(
                Query.query(Criteria.where("numericId").is(numericId)),
                InstanceDocument.class, "instances");
        if (inst == null) throw new InstanceNotFoundException("Instancia no existe: " + numericId);
        return inst;
    }

    private void containerCleanup(InstanceDocument inst) {
        if (inst.getContainerId() == null) return;
        try { dockerClient.stopContainerCmd(inst.getContainerId()).exec(); }
        catch (Exception e) { System.out.println("Stop noop: " + e.getMessage()); }
        try { dockerClient.removeContainerCmd(inst.getContainerId()).withForce(true).exec(); }
        catch (Exception e) { System.out.println("Remove noop: " + e.getMessage()); }
    }

    private String resolveContainerIp(String containerId) {
        for (int attempt = 0; attempt < 5; attempt++) {
            var inspect = dockerClient.inspectContainerCmd(containerId).exec();
            if (inspect.getNetworkSettings() != null && inspect.getNetworkSettings().getNetworks() != null) {
                for (Map.Entry<String, com.github.dockerjava.api.model.ContainerNetwork> entry
                        : inspect.getNetworkSettings().getNetworks().entrySet()) {
                    String ip = entry.getValue().getIpAddress();
                    if (ip != null && !ip.isBlank()) return ip;
                }
            }
            String legacyIp = inspect.getNetworkSettings() != null ? inspect.getNetworkSettings().getIpAddress() : null;
            if (legacyIp != null && !legacyIp.isBlank()) return legacyIp;
            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
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