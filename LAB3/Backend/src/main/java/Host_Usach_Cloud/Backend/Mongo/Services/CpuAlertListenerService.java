package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.CpuMetricsDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.AlertMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class CpuAlertListenerService {

    private static final Logger log = LoggerFactory.getLogger(CpuAlertListenerService.class);
    private static final double CPU_USAGE_THRESHOLD = 75.0;
    private static final String ALERT_TYPE = "CPU_USAGE_HIGH";
    private static final long DUPLICATE_ALERT_WINDOW_MINUTES = 10L;

    private final MessageListenerContainer container;
    private final AlertMongoRepository alertRepository;
    private final AlertService alertService;

    public CpuAlertListenerService(MessageListenerContainer container,
                                   AlertMongoRepository alertRepository,
                                   AlertService alertService) {
        this.container = container;
        this.alertRepository = alertRepository;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
        ChangeStreamRequest<CpuMetricsDocument> request = ChangeStreamRequest.builder()
                .collection("cpu_metrics")
                .filter(Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operationType").is("insert"))
                ))
                .publishTo(message -> {
                    CpuMetricsDocument doc = (CpuMetricsDocument) message.getBody();
                    if (doc != null) {
                        checkAndAlert(doc);
                    }
                })
                .build();

        container.register(request, CpuMetricsDocument.class);
        log.info("Change Stream Listener para CPU registrado exitosamente.");
    }

    private void checkAndAlert(CpuMetricsDocument metrics) {
        if (metrics.getCpuPercent() == null || metrics.getCpuPercent() < CPU_USAGE_THRESHOLD) {
            return;
        }

        Long userId = metrics.getUserId();
        Long instanceId = metrics.getInstanceId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentWindow = now.minusMinutes(DUPLICATE_ALERT_WINDOW_MINUTES);

        if (alertRepository.existsByUserIdAndAlertTypeAndInstanceIdAndTimestampAfter(
                userId, ALERT_TYPE, instanceId, recentWindow)) {
            log.debug("Alerta CPU ya existente para usuario={} instancia={} en los últimos {} minutos. Se omite.",
                    userId, instanceId, DUPLICATE_ALERT_WINDOW_MINUTES);
            return;
        }

        log.warn("Usuario {} instancia {} superó el umbral de CPU ({}%).", userId, instanceId, metrics.getCpuPercent());

        AlertDocument alert = AlertDocument.builder()
                .userId(userId)
                .instanceId(instanceId)
                .alertType(ALERT_TYPE)
                .message(String.format("Uso de CPU muy alto: %.2f%% en la instancia %d.", metrics.getCpuPercent(), instanceId))
                .timestamp(now)
                .read(false)
                .build();

        alertRepository.save(alert);
        alertService.pushAlert(alert);
    }
}
