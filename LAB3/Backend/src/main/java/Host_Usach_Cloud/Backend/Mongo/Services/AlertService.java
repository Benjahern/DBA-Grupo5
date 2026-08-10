package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import Host_Usach_Cloud.Backend.Mongo.Repository.AlertMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
public class AlertService {

    private final AlertMongoRepository alertRepository;

    // Shared sink that broadcasts to all subscribers; each subscriber filters by userId
    private final Sinks.Many<AlertDocument> alertSink = Sinks.many().multicast().onBackpressureBuffer();

    public AlertService(AlertMongoRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Called by BandwidthAlertListenerService when a threshold is exceeded.
     * The alert must already be persisted before calling this.
     */
    public void pushAlert(AlertDocument alert) {
        alertSink.tryEmitNext(alert);
    }

    /**
     * Returns a live stream of new alerts for a given user (used by SSE endpoint).
     * Also replays unread persisted alerts so late subscribers catch up.
     */
    public Flux<AlertDocument> getAlertStreamForUser(Long userId) {
        List<AlertDocument> unread = alertRepository.findByUserIdAndRead(userId, false);

        Flux<AlertDocument> historical = Flux.fromIterable(unread);
        Flux<AlertDocument> live = alertSink.asFlux()
                .filter(alert -> userId.equals(alert.getUserId()));

        return Flux.concat(historical, live);
    }
}
