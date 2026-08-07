package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Services.DTO.BandwidthBucketReport;
import Host_Usach_Cloud.Backend.Services.DTO.BandwidthCostReport;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio que implementa los Aggregation Pipelines de MongoDB para calcular
 * el consumo de ancho de banda y el costo asociado por cliente y por periodo
 * de facturación.
 *
 * <p><b>Pipeline 1</b> — {@link #getConsumptionByClientAndPeriod}: usa {@code $group}
 * para agrupar registros de {@code bandwidth_usage} por {@code (userId, billingPeriod)},
 * sumando bytes y calculando el costo con pricing escalonado.</p>
 *
 * <p><b>Pipeline 2</b> — {@link #getDistributionByBucket}: usa {@code $group} +
 * {@code $bucket} para clasificar clientes en rangos de consumo.</p>
 */
@Service
public class BandwidthAggregationService {

    private static final Logger log = LoggerFactory.getLogger(BandwidthAggregationService.class);

    private static final String COLLECTION = "bandwidth_usage";

    /** Constante: 1 GB en bytes */
    private static final double GB = 1024.0 * 1024.0 * 1024.0;

    private final MongoTemplate mongoTemplate;

    public BandwidthAggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Pipeline 1: Consumo y costo por cliente y periodo ($group)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Aggregation Pipeline que calcula el consumo de ancho de banda y el costo
     * asociado por cliente y por periodo de facturación.
     *
     * <pre>
     * Pipeline:
     *   $match  → filtra por billingPeriod (y opcionalmente userId)
     *   $group  → agrupa por {userId, billingPeriod}, suma bytesIn/bytesOut/totalBytes,
     *             cuenta registros, acumula instanceIds distintos
     *   $addFields → calcula totalGb, bandwidthCost (pricing escalonado), instanceCount
     *   $sort   → ordena por totalBytes descendente
     * </pre>
     *
     * @param period periodo de facturación en formato "YYYY-MM"
     * @param userId filtro opcional por usuario (null = todos)
     * @return lista de reportes de consumo y costo
     */
    public List<BandwidthCostReport> getConsumptionByClientAndPeriod(String period, Long userId) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // ── Stage 1: $match ─────────────────────────────────────────────
        // Filtra por periodo de facturación y opcionalmente por userId
        Criteria matchCriteria = Criteria.where("billingPeriod").is(period);
        if (userId != null) {
            matchCriteria = matchCriteria.and("userId").is(userId);
        }
        pipeline.add(Aggregation.match(matchCriteria));

        // ── Stage 2: $group ─────────────────────────────────────────────
        // Agrupa por (userId, billingPeriod):
        //   - Suma bytesIn, bytesOut, totalBytes
        //   - Cuenta registros
        //   - Acumula conjunto de instanceId distintos con $addToSet
        pipeline.add(Aggregation.group("userId", "billingPeriod")
                .sum("bytesIn").as("totalBytesIn")
                .sum("bytesOut").as("totalBytesOut")
                .sum("totalBytes").as("totalBytes")
                .count().as("recordCount")
                .addToSet("instanceId").as("distinctInstances"));

        // ── Stage 3: $addFields ─────────────────────────────────────────
        // Calcula:
        //   - totalGb = totalBytes / (1024^3)
        //   - instanceCount = $size de distinctInstances
        //   - bandwidthCost = pricing escalonado con $switch:
        //       0-10 GB   → $0.00/GB (gratis)
        //       10-100 GB → $0.05/GB (solo la porción que excede 10 GB)
        //       100-1000  → $0.03/GB (solo la porción que excede 100 GB)
        //       1000+ GB  → $0.01/GB (solo la porción que excede 1000 GB)
        //
        // Fórmula acumulativa:
        //   cost = max(0, min(totalGb, 100) - 10) * 0.05
        //        + max(0, min(totalGb, 1000) - 100) * 0.03
        //        + max(0, totalGb - 1000) * 0.01
        Document totalGbExpr = new Document("$divide", Arrays.asList("$totalBytes", GB));

        // Tramo 1: (min(totalGb, 100) - 10) * 0.05, con floor en 0
        Document tramo1 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(
                                new Document("$min", Arrays.asList(totalGbExpr, 100.0)),
                                10.0)))),
                0.05));

        // Tramo 2: (min(totalGb, 1000) - 100) * 0.03, con floor en 0
        Document tramo2 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(
                                new Document("$min", Arrays.asList(totalGbExpr, 1000.0)),
                                100.0)))),
                0.03));

        // Tramo 3: (totalGb - 1000) * 0.01, con floor en 0
        Document tramo3 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(totalGbExpr, 1000.0)))),
                0.01));

        Document bandwidthCostExpr = new Document("$round", Arrays.asList(
                new Document("$add", Arrays.asList(tramo1, tramo2, tramo3)),
                2));

        pipeline.add(context -> new Document("$addFields", new Document()
                .append("totalGb", new Document("$round", Arrays.asList(totalGbExpr, 4)))
                .append("instanceCount", new Document("$size", "$distinctInstances"))
                .append("bandwidthCost", bandwidthCostExpr)));

        // ── Stage 4: $sort ──────────────────────────────────────────────
        pipeline.add(Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "totalBytes"));

        // ── Ejecutar ────────────────────────────────────────────────────
        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, Document.class);

        List<BandwidthCostReport> reports = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Document idDoc = doc.get("_id", Document.class);
            reports.add(BandwidthCostReport.builder()
                    .userId(toLong(idDoc.get("userId")))
                    .billingPeriod(idDoc.getString("billingPeriod"))
                    .totalBytesIn(toLong(doc.get("totalBytesIn")))
                    .totalBytesOut(toLong(doc.get("totalBytesOut")))
                    .totalBytes(toLong(doc.get("totalBytes")))
                    .totalGb(toDouble(doc.get("totalGb")))
                    .bandwidthCost(toDouble(doc.get("bandwidthCost")))
                    .recordCount(toInt(doc.get("recordCount")))
                    .instanceCount(toInt(doc.get("instanceCount")))
                    .build());
        }

        log.info("Pipeline 1 ($group): periodo={}, userId={}, resultados={}", period, userId, reports.size());
        return reports;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Pipeline 2: Distribución por rangos de consumo ($bucket)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Aggregation Pipeline que clasifica a los clientes en rangos de consumo
     * de ancho de banda usando {@code $bucket}.
     *
     * <pre>
     * Pipeline:
     *   $match  → filtra por billingPeriod
     *   $group  → pre-agrupa totalBytes por userId
     *   $addFields → calcula totalGb a partir de totalBytes
     *   $bucket → clasifica en rangos:
     *              [0, 1) GB    → uso bajo
     *              [1, 10) GB   → uso moderado
     *              [10, 100) GB → uso alto
     *              [100, 1000) GB → uso intensivo
     *              [1000, +∞)   → enterprise
     * </pre>
     *
     * @param period periodo de facturación en formato "YYYY-MM"
     * @return lista de reportes por rango de consumo
     */
    public List<BandwidthBucketReport> getDistributionByBucket(String period) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // ── Stage 1: $match ─────────────────────────────────────────────
        pipeline.add(Aggregation.match(Criteria.where("billingPeriod").is(period)));

        // ── Stage 2: $group ─────────────────────────────────────────────
        // Pre-agrupa totalBytes por userId para tener un total por cliente
        pipeline.add(Aggregation.group("userId")
                .sum("totalBytes").as("totalBytes"));

        // ── Stage 3: $addFields ─────────────────────────────────────────
        // Convierte totalBytes a GB para el bucketeo
        Document totalGbExpr = new Document("$divide", Arrays.asList("$totalBytes", GB));

        // Calcula el costo con la misma fórmula escalonada del pipeline 1
        Document tramo1 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(
                                new Document("$min", Arrays.asList(totalGbExpr, 100.0)),
                                10.0)))),
                0.05));
        Document tramo2 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(
                                new Document("$min", Arrays.asList(totalGbExpr, 1000.0)),
                                100.0)))),
                0.03));
        Document tramo3 = new Document("$multiply", Arrays.asList(
                new Document("$max", Arrays.asList(0.0,
                        new Document("$subtract", Arrays.asList(totalGbExpr, 1000.0)))),
                0.01));
        Document costExpr = new Document("$round", Arrays.asList(
                new Document("$add", Arrays.asList(tramo1, tramo2, tramo3)), 2));

        pipeline.add(context -> new Document("$addFields", new Document()
                .append("totalGb", new Document("$round", Arrays.asList(totalGbExpr, 4)))
                .append("cost", costExpr)));

        // ── Stage 4: $bucket ────────────────────────────────────────────
        // Clasifica clientes en rangos de consumo por totalGb.
        // boundaries: [0, 1, 10, 100, 1000, Infinity)
        // Para cada bucket acumula: cantidad de clientes, total de GB, total de costo
        Document bucketStage = new Document("$bucket", new Document()
                .append("groupBy", "$totalGb")
                .append("boundaries", Arrays.asList(0.0, 1.0, 10.0, 100.0, 1000.0))
                .append("default", "enterprise")
                .append("output", new Document()
                        .append("clientCount", new Document("$sum", 1))
                        .append("totalGbInBucket", new Document("$sum", "$totalGb"))
                        .append("totalCostInBucket", new Document("$sum", "$cost"))));

        pipeline.add(context -> bucketStage);

        // ── Ejecutar ────────────────────────────────────────────────────
        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, Document.class);

        // Labels para los rangos
        String[] labels = {"0 – 1 GB", "1 – 10 GB", "10 – 100 GB", "100 – 1,000 GB", "1,000+ GB"};
        double[][] ranges = {{0, 1}, {1, 10}, {10, 100}, {100, 1000}, {1000, Double.MAX_VALUE}};

        List<BandwidthBucketReport> reports = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object idRaw = doc.get("_id");
            int rangeIndex = resolveRangeIndex(idRaw);

            reports.add(BandwidthBucketReport.builder()
                    .bucketMinGb(rangeIndex >= 0 && rangeIndex < ranges.length ? ranges[rangeIndex][0] : 1000.0)
                    .bucketMaxGb(rangeIndex >= 0 && rangeIndex < ranges.length
                            ? (ranges[rangeIndex][1] == Double.MAX_VALUE ? null : ranges[rangeIndex][1])
                            : null)
                    .bucketLabel(rangeIndex >= 0 && rangeIndex < labels.length ? labels[rangeIndex] : "1,000+ GB")
                    .clientCount(toInt(doc.get("clientCount")))
                    .totalGbInBucket(toDouble(doc.get("totalGbInBucket")))
                    .totalCostInBucket(toDouble(doc.get("totalCostInBucket")))
                    .build());
        }

        log.info("Pipeline 2 ($bucket): periodo={}, rangos={}", period, reports.size());
        return reports;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers de conversión segura de tipos
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Resuelve el índice del rango según el _id del bucket.
     * Si el _id es numérico, corresponde al boundary; si es "enterprise", es el último.
     */
    private int resolveRangeIndex(Object idRaw) {
        if (idRaw instanceof String && "enterprise".equals(idRaw)) return 4;
        double val = toDouble(idRaw);
        if (val < 1.0) return 0;
        if (val < 10.0) return 1;
        if (val < 100.0) return 2;
        if (val < 1000.0) return 3;
        return 4;
    }

    private static Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Double) return ((Double) o).longValue();
        return Long.parseLong(o.toString());
    }

    private static Double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Double) return (Double) o;
        if (o instanceof Long) return ((Long) o).doubleValue();
        if (o instanceof Integer) return ((Integer) o).doubleValue();
        return Double.parseDouble(o.toString());
    }

    private static Integer toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Double) return ((Double) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
