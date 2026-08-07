package Host_Usach_Cloud.Backend.Mongo.Config;

import Host_Usach_Cloud.Backend.Mongo.Entity.BandwidthUsageDocument;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeder idempotente que genera datos de prueba de consumo de ancho de banda
 * para que los Aggregation Pipelines tengan data real para demostrar.
 *
 * <p>Solo ejecuta si la colección {@code bandwidth_usage} está vacía.
 * Genera registros para los últimos 3 meses con patrones realistas:
 * distintos usuarios, distintas instancias, variación de consumo.</p>
 *
 * <p>Funciona en dos modos:</p>
 * <ul>
 *   <li>Si hay instancias en MongoDB, usa esos userId/numericId reales</li>
 *   <li>Si no hay instancias, genera datos sintéticos para 3 usuarios ficticios</li>
 * </ul>
 */
@Component
@Order(100)  // Ejecutar después de MongoSchemaInitializer
public class BandwidthDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BandwidthDataSeeder.class);
    private static final String COLLECTION = "bandwidth_usage";

    private final MongoTemplate mongoTemplate;

    public BandwidthDataSeeder(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        long count = mongoTemplate.count(new Query(), COLLECTION);
        if (count > 0) {
            log.info("BandwidthDataSeeder: colección '{}' ya tiene {} documentos — no-op.", COLLECTION, count);
            return;
        }

        log.info("BandwidthDataSeeder: colección vacía, generando datos de prueba (reducidos)...");

        // Buscar instancias reales en MongoDB
        List<InstanceDocument> instances = mongoTemplate.findAll(InstanceDocument.class, "instances");

        List<BandwidthUsageDocument> docs = new ArrayList<>();
        Random rng = new Random(42); // seed fijo para reproducibilidad

        if (!instances.isEmpty()) {
            // ── Modo real: usar instancias existentes ──────────────────
            for (InstanceDocument inst : instances) {
                generateRecordsForInstance(docs, rng,
                        inst.getUserId(), inst.getNumericId());
            }
        } else {
            // ── Modo sintético: simular 3 usuarios con 2 instancias c/u ──
            log.info("BandwidthDataSeeder: no hay instancias en Mongo, generando datos sintéticos.");
            long[][] syntheticUsers = {
                    {1L, 1L}, {1L, 2L},   // usuario 1, instancias 1 y 2
                    {2L, 1L}, {2L, 2L},   // usuario 2, instancias 1 y 2
                    {3L, 1L},             // usuario 3, instancia 1
            };
            for (long[] pair : syntheticUsers) {
                generateRecordsForInstance(docs, rng, pair[0], pair[1]);
            }
        }

        if (!docs.isEmpty()) {
            mongoTemplate.insertAll(docs);
            log.info("BandwidthDataSeeder: {} registros de bandwidth_usage generados.", docs.size());
        }
    }

    /**
     * Genera registros de ancho de banda para una instancia específica
     * durante los últimos 3 meses, con ~6 muestras/día (cada 4 horas).
     */
    private void generateRecordsForInstance(List<BandwidthUsageDocument> docs,
                                            Random rng,
                                            Long userId, Long instanceId) {
        YearMonth currentMonth = YearMonth.now();

        // Patrones de uso base por usuario (bytes por muestra)
        // Usuario 1: alto consumo (servidor web con mucho tráfico)
        // Usuario 2: consumo medio (aplicación interna)
        // Usuario 3: bajo consumo (servidor de desarrollo)
        long baseRx, baseTx;
        switch (userId.intValue() % 3) {
            case 1:
                baseRx = 500_000_000L;  // ~500 MB rx por muestra
                baseTx = 200_000_000L;  // ~200 MB tx por muestra
                break;
            case 2:
                baseRx = 50_000_000L;   // ~50 MB rx
                baseTx = 30_000_000L;   // ~30 MB tx
                break;
            default:
                baseRx = 5_000_000L;    // ~5 MB rx
                baseTx = 2_000_000L;    // ~2 MB tx
                break;
        }

        // Generar para los últimos 3 meses
        for (int monthOffset = 2; monthOffset >= 0; monthOffset--) {
            YearMonth ym = currentMonth.minusMonths(monthOffset);
            String period = ym.toString();
            int daysInMonth = ym.lengthOfMonth();

            // Limitar a días que ya han pasado para el mes actual
            int maxDay = (monthOffset == 0)
                    ? Math.min(daysInMonth, LocalDateTime.now().getDayOfMonth())
                    : daysInMonth;

            // Generar solo 1 muestra (consolidada) por mes para no saturar de datos dummy
            int day = 1 + rng.nextInt(maxDay > 0 ? maxDay : 1);
            int hour = 12; // mediodía
            LocalDateTime ts = ym.atDay(day).atTime(hour, 0, 0);

            // Variación aleatoria ±50%
            double factor = 0.5 + rng.nextDouble();
            // Aumentar los bases para que 1 solo registro represente el consumo del mes entero
            long rx = (long) (baseRx * factor * 100); 
            long tx = (long) (baseTx * factor * 100);

            docs.add(BandwidthUsageDocument.builder()
                    .userId(userId)
                    .instanceId(instanceId)
                    .bytesIn(rx)
                    .bytesOut(tx)
                    .totalBytes(rx + tx)
                    .timestamp(ts)
                    .billingPeriod(period)
                    .build());
        }
    }
}
