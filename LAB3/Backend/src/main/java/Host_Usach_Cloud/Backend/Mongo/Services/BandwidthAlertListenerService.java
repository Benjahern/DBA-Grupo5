package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.BandwidthUsageDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.ClientQuotaDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.AlertMongoRepository;
import Host_Usach_Cloud.Backend.Mongo.Repository.ClientQuotaMongoRepository;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BandwidthAlertListenerService {
    private static final Logger log = LoggerFactory.getLogger(BandwidthAlertListenerService.class);

    /** Umbral por defecto (10 GB) cuando el plan del usuario no define uno. */
    private static final long DEFAULT_THRESHOLD_BYTES = (long) 10 * 1024 * 1024 * 1024;

    private static final String ALERT_TYPE = "BANDWIDTH_QUOTA_EXCEEDED";

    private final MessageListenerContainer container;
    private final MongoTemplate mongoTemplate;
    private final AlertMongoRepository alertRepository;
    private final ClientQuotaMongoRepository clientQuotaRepository;
    private final AlertService alertService;

    public BandwidthAlertListenerService(MessageListenerContainer container,
                                         MongoTemplate mongoTemplate,
                                         AlertMongoRepository alertRepository,
                                         ClientQuotaMongoRepository clientQuotaRepository,
                                         AlertService alertService) {
        this.container = container;
        this.mongoTemplate = mongoTemplate;
        this.alertRepository = alertRepository;
        this.clientQuotaRepository = clientQuotaRepository;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
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

        container.register(request, BandwidthUsageDocument.class);
        log.info("Change Stream Listener para ancho de banda registrado exitosamente.");
    }

    private void checkAndAlertUser(BandwidthUsageDocument newUsage) {
        Long userId = newUsage.getUserId();
        String period = newUsage.getBillingPeriod();

        // Agregación en MongoDB: suma totalBytes del usuario en el periodo actual
        Aggregation sumAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId).and("billingPeriod").is(period)),
                Aggregation.group().sum("totalBytes").as("totalConsumed")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(sumAgg, "bandwidth_usage", Document.class);
        Document resultDoc = results.getUniqueMappedResult();

        long totalConsumed = 0L;
        if (resultDoc != null && resultDoc.get("totalConsumed") != null) {
            totalConsumed = resultDoc.get("totalConsumed", Number.class).longValue();
        }

        // Umbral del plan del usuario; si no está configurado se usa el default
        long threshold = Optional.ofNullable(clientQuotaRepository.findById(userId).orElse(null))
                .map(ClientQuotaDocument::getMaxBandwidthBytes)
                .filter(v -> v != null && v > 0)
                .orElse(DEFAULT_THRESHOLD_BYTES);

        if (totalConsumed < threshold) {
            return;
        }

        // Evitar duplicados: una sola alerta por usuario y periodo de facturación
        if (alertRepository.existsByUserIdAndAlertTypeAndBillingPeriod(userId, ALERT_TYPE, period)) {
            log.debug("Alerta de ancho de banda ya existente para usuario={} periodo={}. Se omite.", userId, period);
            return;
        }

        log.warn("Usuario {} superó el umbral de ancho de banda del plan ({} bytes) en periodo {}.",
                userId, threshold, period);

        AlertDocument alert = AlertDocument.builder()
                .userId(userId)
                .alertType(ALERT_TYPE)
                .message(String.format(
                        "Has superado el umbral de consumo de ancho de banda mensual (%.2f GB).",
                        threshold / (1024.0 * 1024.0 * 1024.0)))
                .timestamp(LocalDateTime.now())
                .billingPeriod(period)
                .read(false)
                .build();

        mongoTemplate.save(alert);
        alertService.pushAlert(alert);
    }
}
