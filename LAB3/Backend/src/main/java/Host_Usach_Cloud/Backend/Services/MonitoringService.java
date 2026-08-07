package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Consumption;
import Host_Usach_Cloud.Backend.Mongo.Entity.BandwidthUsageDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.InstanceMongoRepository;
import Host_Usach_Cloud.Backend.Repository.ConsumptionRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StatisticNetworksConfig;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MonitoringService {

    private final DockerClient dockerClient;
    private final InstanceMongoRepository instanceMongoRepository;
    private final ConsumptionRepository consumptionRepository;
    private final MongoTemplate mongoTemplate;

    public MonitoringService(DockerClient dockerClient,
                             InstanceMongoRepository instanceMongoRepository,
                             ConsumptionRepository consumptionRepository,
                             MongoTemplate mongoTemplate) {
        this.dockerClient = dockerClient;
        this.instanceMongoRepository = instanceMongoRepository;
        this.consumptionRepository = consumptionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(fixedRate = 600000) // 10 minutos
    public void logContainerStats() {
        List<InstanceDocument> runningInstances = instanceMongoRepository.findByState("Running");

        for (InstanceDocument instance : runningInstances) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);
                StatsCmd statsCmd = dockerClient.statsCmd(instance.getContainerId());

                ResultCallback<Statistics> callback = new ResultCallback.Adapter<Statistics>() {
                    @Override
                    public void onNext(Statistics stats) {
                        if (stats != null) {
                            double cpuUsage = 0.0;
                            if (stats.getCpuStats() != null && stats.getCpuStats().getCpuUsage() != null && stats.getPreCpuStats() != null) {
                                long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() - stats.getPreCpuStats().getCpuUsage().getTotalUsage();
                                long systemCpuDelta = stats.getCpuStats().getSystemCpuUsage() - stats.getPreCpuStats().getSystemCpuUsage();
                                Long onlineCpus = stats.getCpuStats().getOnlineCpus();
                                long onlineCpusValue = onlineCpus != null ? onlineCpus : 1L;
                                if (systemCpuDelta > 0 && cpuDelta > 0) {
                                    cpuUsage = (double) cpuDelta / (double) systemCpuDelta * onlineCpusValue * 100.0;
                                }
                            }

                            double ramUsage = 0.0;
                            if (stats.getMemoryStats() != null && stats.getMemoryStats().getUsage() != null) {
                                ramUsage = stats.getMemoryStats().getUsage() / (1024.0 * 1024.0); // a MB
                            }

                            // ── Persistir métricas CPU/RAM/Storage en Postgres (lógica existente) ──
                            Consumption consumption = Consumption.builder()
                                    .Instance_id(instance.getNumericId())    // Long sequential per-user
                                    .Cpu_stats(cpuUsage)
                                    .Ram_stats(ramUsage)
                                    .Storage_stats(0.0)
                                    .Created_at(LocalDateTime.now())
                                    .build();
                            consumptionRepository.save(consumption);

                            // ── Persistir ancho de banda en MongoDB (NUEVO) ──────────────────
                            // Lectura de networkRx/networkTx desde Docker stats
                            long networkRx = 0L;
                            long networkTx = 0L;
                            if (stats.getNetworks() != null) {
                                for (Map.Entry<String, StatisticNetworksConfig> entry : stats.getNetworks().entrySet()) {
                                    StatisticNetworksConfig netConfig = entry.getValue();
                                    if (netConfig != null) {
                                        networkRx += netConfig.getRxBytes() != null ? netConfig.getRxBytes() : 0L;
                                        networkTx += netConfig.getTxBytes() != null ? netConfig.getTxBytes() : 0L;
                                    }
                                }
                            }

                            LocalDateTime now = LocalDateTime.now();
                            BandwidthUsageDocument bwDoc = BandwidthUsageDocument.builder()
                                    .userId(instance.getUserId())
                                    .instanceId(instance.getNumericId())
                                    .bytesIn(networkRx)
                                    .bytesOut(networkTx)
                                    .totalBytes(networkRx + networkTx)
                                    .timestamp(now)
                                    .billingPeriod(YearMonth.from(now).toString())
                                    .build();
                            try {
                                mongoTemplate.insert(bwDoc, "bandwidth_usage");
                            } catch (Exception bwEx) {
                                System.err.println("Error guardando bandwidth_usage: " + bwEx.getMessage());
                            }

                            try { close(); } catch (IOException e) {
                                System.err.println("Error cerrando el callback de stats: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) { latch.countDown(); }
                    @Override
                    public void onComplete() { latch.countDown(); }
                };

                statsCmd.exec(callback);
                latch.await(5, TimeUnit.SECONDS);

            } catch (Exception e) {
                System.err.println("Error obteniendo stats para la instancia " + instance.getInstanceId() + ": " + e.getMessage());
            }
        }
    }

    /** Conversión segura a long para valores numéricos de la API Docker */
    private static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Double) return ((Double) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }
}