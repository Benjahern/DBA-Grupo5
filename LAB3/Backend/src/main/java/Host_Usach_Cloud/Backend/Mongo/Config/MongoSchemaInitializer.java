package Host_Usach_Cloud.Backend.Mongo.Config;

import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;

import java.util.Arrays;
import java.util.List;

@Component
public class MongoSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoSchemaInitializer.class);

    private final MongoTemplate mongoTemplate;

    public MongoSchemaInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        MongoDatabase db = mongoTemplate.getDb();
        migrateInstanceFields(db);
        dropStaleIndexes(db);
        ensureInstancesCollection(db);
        ensureClientQuotasCollection(db);
        ensureBandwidthUsageCollection(db);
        backfillNumericIds();
    }

    /**
     * Migración idempotente de documentos pre-existentes al esquema
     * renombrado (state→State, startedAt→Started_at, activeHoursSeconds→Active_hours).
     * Filtra por {state: {$exists: true}} — un documento ya migrado no
     * tiene el campo viejo, así que el filtro no matchea y el updateMany
     * es un no-op en arranques posteriores.
     *
     * <p>Debe correr ANTES de aplicar el validator estricto, porque el
     * validator viejo aún requiere los campos con nombres antiguos. Se
     * relaja temporalmente con collMod → validationAction:warn.</p>
     */
    private void migrateInstanceFields(MongoDatabase db) {
        try {
            // 1) Relajar validator para que el updateMany no falle contra el schema viejo
            db.runCommand(BsonDocument.parse(new Document("collMod", "instances")
                    .append("validationAction", "warn")
                    .append("validationLevel", "moderate")
                    .toJson()));

            // 2) updateMany con pipeline que renombra + convierte unidades + borra
            db.getCollection("instances").updateMany(
                    new Document("state", new Document("$exists", true)),
                    Arrays.asList(
                            new Document("$set", new Document()
                                    .append("State", "$state")
                                    .append("Started_at", "$startedAt")
                                    .append("Active_hours", new Document("$divide", Arrays.asList(
                                            new Document("$toDouble", new Document("$ifNull",
                                                    Arrays.asList("$activeHoursSeconds", 0L))),
                                            3600.0)))),
                            new Document("$unset", Arrays.asList("state", "startedAt", "activeHoursSeconds"))
                    )
            );

            long legacy = db.getCollection("instances")
                    .countDocuments(new Document("state", new Document("$exists", true)));
            if (legacy > 0) {
                log.warn("Mongo: la migración instanceFields no vació el filtro legacy ({} docs).", legacy);
            } else {
                log.info("Mongo: migración instanceFields aplicada (state→State, startedAt→Started_at, activeHoursSeconds/3600→Active_hours).");
            }
        } catch (Exception e) {
            log.warn("Mongo: migración instanceFields falló: {}", e.getMessage());
        }
    }

    /**
     * Drop índices del esquema viejo que ya no se referencian desde
     * InstanceDocument. Reusar el nombre con un key spec distinto lanza
     * IndexOptionsConflict durante el context refresh — antes de que
     * cualquier runner pueda borrarlo. Por eso se borra explícitamente.
     */
    private void dropStaleIndexes(MongoDatabase db) {
        dropIndexIfExists(db, "instances", "state_1");
        dropIndexIfExists(db, "instances", "user_state_idx");
    }

    private void dropIndexIfExists(MongoDatabase db, String coll, String indexName) {
        try {
            db.runCommand(BsonDocument.parse(
                    new Document("dropIndexes", coll).append("index", indexName).toJson()));
            log.info("Mongo: índice '{}' dropeado de '{}'.", indexName, coll);
        } catch (Exception e) {
            // IndexNotFound (code 27) y NamespaceNotFound (code 26) son esperados en arranque limpio.
            log.debug("Mongo: dropIndexes '{}'.'{}' no aplicado: {}", coll, indexName, e.getMessage());
        }
    }

    /**
     * Backfill idempotente: para instancias que aún no tienen numericId
     * (data vieja de antes del cambio a IDs numéricos), asigna uno
     * sequential per-user ordenado por {@code Started_at}. Si la instancia
     * ya tiene numericId, no la toca — corre en cada arranque pero es no-op
     * una vez que toda la data está al día.
     */
    private void backfillNumericIds() {
        try {
            List<Long> distinctUsers = mongoTemplate.findDistinct(
                    Query.query(Criteria.where("numericId").is(null)),
                    "userId", InstanceDocument.class, Long.class);

            int totalAssigned = 0;
            for (Long userId : distinctUsers) {
                List<InstanceDocument> userInstances = mongoTemplate.find(
                        Query.query(Criteria.where("userId").is(userId).and("numericId").is(null))
                                .with(Sort.by(Sort.Direction.ASC, "startedAt")),
                        InstanceDocument.class, "instances");
                long next = 1L;
                for (InstanceDocument inst : userInstances) {
                    mongoTemplate.updateFirst(
                            Query.query(Criteria.where("_id").is(inst.getInstanceId())),
                            new Update().set("numericId", next),
                            InstanceDocument.class, "instances");
                    next++;
                    totalAssigned++;
                }
            }
            if (totalAssigned > 0) {
                log.info("Mongo: backfill de numericId asignó {} instancia(s).", totalAssigned);
            }
        } catch (Exception e) {
            log.warn("Mongo: backfill de numericId falló: {}", e.getMessage());
        }
    }

    private void ensureInstancesCollection(MongoDatabase db) {
        String name = "instances";
        Document validator = new Document("$jsonSchema", new Document()
                .append("bsonType", "object")
                .append("required", List.of(
                        "name", "State", "userId",
                        "cpuId", "ramId",
                        "Started_at", "terminated", "Active_hours"))
                .append("properties", new Document()
                        .append("name", new Document("bsonType", "string").append("minLength", 1))
                        .append("State", new Document("enum", List.of("Running", "Stopped", "Terminated")))
                        .append("userId", new Document("bsonType", "long"))
                        .append("cpuId", new Document("bsonType", "long"))
                        .append("ramId", new Document("bsonType", "long"))
                        .append("storageId", new Document("bsonType", "long"))
                        .append("regionId", new Document("bsonType", "long"))
                        .append("datacenterId", new Document("bsonType", "long"))
                        .append("containerId", new Document("bsonType", List.of("string", "null")))
                        .append("Started_at", new Document("bsonType", List.of("date", "null")))
                        .append("Active_hours", new Document("bsonType", List.of("double", "int", "long"))
                                .append("minimum", 0))
                        .append("terminated", new Document("bsonType", "bool"))
                        .append("ipAddress", new Document("bsonType", List.of("string", "null")))
                        .append("color", new Document("bsonType", List.of("string", "null")))
                ));
        applyValidator(db, name, validator);
    }

    private void ensureClientQuotasCollection(MongoDatabase db) {
        String name = "client_quotas";
        Document validator = new Document("$jsonSchema", new Document()
                .append("bsonType", "object")
                .append("required", List.of("maxInstances", "activeCount"))
                .append("properties", new Document()
                        .append("maxInstances", new Document("bsonType", "int").append("minimum", 0))
                        .append("activeCount", new Document("bsonType", "int").append("minimum", 0))
                ));
        applyValidator(db, name, validator);
    }

    private void ensureBandwidthUsageCollection(MongoDatabase db) {
        String name = "bandwidth_usage";
        Document validator = new Document("$jsonSchema", new Document()
                .append("bsonType", "object")
                .append("required", List.of(
                        "userId", "instanceId", "bytesIn", "bytesOut",
                        "totalBytes", "timestamp", "billingPeriod"))
                .append("properties", new Document()
                        .append("userId", new Document("bsonType", "long"))
                        .append("instanceId", new Document("bsonType", "long"))
                        .append("bytesIn", new Document("bsonType", "long").append("minimum", 0))
                        .append("bytesOut", new Document("bsonType", "long").append("minimum", 0))
                        .append("totalBytes", new Document("bsonType", "long").append("minimum", 0))
                        .append("timestamp", new Document("bsonType", "date"))
                        .append("billingPeriod", new Document("bsonType", "string")
                                .append("pattern", "^\\d{4}-\\d{2}$"))
                ));
        applyValidator(db, name, validator);
    }

    /**
     * Crea la colección con validator si no existe; si ya existe, aplica
     * collMod para fijar/refrescar el validator. Importante: Spring Data
     * Mongo con auto-index-creation puede crear colecciones implícitamente
     * antes de que este initializer corra — por eso siempre usamos collMod.
     */
    private void applyValidator(MongoDatabase db, String name, Document validator) {
        if (!collectionExists(db, name)) {
            Document cmd = new Document("create", name)
                    .append("validator", validator)
                    .append("validationLevel", "strict")
                    .append("validationAction", "error");
            try {
                db.runCommand(BsonDocument.parse(cmd.toJson()));
                log.info("Mongo: colección '{}' creada con Schema Validation estricta.", name);
                return;
            } catch (Exception e) {
                log.warn("Mongo: no se pudo crear '{}': {}", name, e.getMessage());
            }
        }
        // Si ya existe (creada por auto-index), aplicar/refrescar validator con collMod
        Document collMod = new Document("collMod", name)
                .append("validator", validator)
                .append("validationLevel", "strict")
                .append("validationAction", "error");
        try {
            db.runCommand(BsonDocument.parse(collMod.toJson()));
            log.info("Mongo: validator aplicado via collMod a '{}'.", name);
        } catch (Exception e) {
            log.warn("Mongo: no se pudo aplicar validator a '{}': {}", name, e.getMessage());
        }
    }

    private boolean collectionExists(MongoDatabase db, String name) {
        for (String n : db.listCollectionNames()) {
            if (n.equals(name)) return true;
        }
        return false;
    }
}