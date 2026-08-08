package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.BandwidthUsageDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.BandwidthUsageRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BandwidthAlertListenerService {
    private static final Logger log = LoggerFactory.getLogger(BandwidthAlertListenerService.class);

    private final MessageListenerContainer container;
    private final MongoTemplate mongoTemplate;
    private final BandwidthUsageRepository bandwidthRepository;
    private final AlertService alertService;

    // Umbral de ejemplo: 10 GB en Bytes
    private static final long BANDWIDTH_THRESHOLD = 10L * 1024 * 1024 * 1024;

    public BandwidthAlertListenerService(MessageListenerContainer container,
                                         MongoTemplate mongoTemplate,
                                         BandwidthUsageRepository bandwidthRepository,
                                         AlertService alertService) {
        this.container = container;
        this.mongoTemplate = mongoTemplate;
        this.bandwidthRepository = bandwidthRepository;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
        // Configuramos el request para escuchar solo inserciones en bandwidth_usage
        ChangeStreamRequest<BandwidthUsageDocument> request = ChangeStreamRequest.builder()
                .collection("bandwidth_usage")
                .filter(Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operationType").is("insert"))
                ))
                .publishTo(message -> {
                    BandwidthUsageDocument doc = (BandwidthUsageDocument) message.getBody();
                    if (doc != null) {
                        checkAndAlertUser(doc);
                    }
                })
                .build();

        // Registramos el listener al contenedor
        container.register(request, BandwidthUsageDocument.class);
        log.info("Change Stream Listener para ancho de banda registrado exitosamente.");
    }

    private void checkAndAlertUser(BandwidthUsageDocument newUsage) {
        Long userId = newUsage.getUserId();
        String period = newUsage.getBillingPeriod();

        // Obtener la suma total de consumo del cliente en este periodo
        // También puedes reutilizar el BandwidthAggregationService que ya tienes
        long totalConsumed = bandwidthRepository.findByUserIdAndBillingPeriod(userId, period)
                .stream()
                .mapToLong(BandwidthUsageDocument::getTotalBytes)
                .sum();

        if (totalConsumed >= BANDWIDTH_THRESHOLD) {
            log.warn("El usuario {} ha superado el límite de ancho de banda del plan.", userId);

            // 1. Actualizar/Crear la colección materializada de alertas
            AlertDocument alert = AlertDocument.builder()
                    .userId(userId)
                    .alertType("BANDWIDTH_QUOTA_EXCEEDED")
                    .message("Has superado el umbral de consumo de ancho de banda mensual.")
                    .timestamp(LocalDateTime.now())
                    .read(false)
                    .build();

            mongoTemplate.save(alert);

            // 2. Notificar al usuario en tiempo real vía SSE
            alertService.pushAlert(alert);
        }
    }
}
