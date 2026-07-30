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
        ensureInstancesCollection(db);
        ensureClientQuotasCollection(db);
        backfillNumericIds();
    }

    /**
     * Backfill idempotente: para instancias que aún no tienen numericId
     * (data vieja de antes del cambio a IDs numéricos), asigna uno
     * sequential per-user ordenado por {@code startedAt}. Si la instancia
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
                            "instances");
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
                        "name", "state", "userId",
                        "cpuId", "ramId",
                        "startedAt", "terminated", "activeHoursSeconds"))
                .append("properties", new Document()
                        .append("name", new Document("bsonType", "string").append("minLength", 1))
                        .append("state", new Document("enum", List.of("Running", "Stopped", "Terminated")))
                        .append("userId", new Document("bsonType", "long"))
                        .append("cpuId", new Document("bsonType", "long"))
                        .append("ramId", new Document("bsonType", "long"))
                        .append("storageId", new Document("bsonType", "long"))
                        .append("regionId", new Document("bsonType", "long"))
                        .append("datacenterId", new Document("bsonType", "long"))
                        .append("containerId", new Document("bsonType", List.of("string", "null")))
                        .append("startedAt", new Document("bsonType", List.of("date", "null")))
                        .append("activeHoursSeconds", new Document("bsonType", "long").append("minimum", 0))
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