package Host_Usach_Cloud.Backend.Mongo.Services;

import Host_Usach_Cloud.Backend.Mongo.Exceptions.QuotaExceededException;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cuota por cliente (Req 2 del lab).
 *
 * El check atómico + incremento se hace sobre la colección `client_quotas` con
 * un filter que incluye $expr comparando activeCount vs maxInstances. Esto
 * reemplaza al trigger Postgres `trg_check_quota`. MongoDB Schema Validation
 * ($jsonSchema) no puede contar entre documentos, por eso usamos este patrón.
 *
 * Importante: reserve() debe ejecutarse dentro de una transacción Mongo
 * (TxTemplate) cuando se llama desde InstanceService.createInstance, para que
 * la reserva de cuota y el insert de la instancia sean atómicos. La transacción
 * se gestiona en InstanceService — este bean sólo provee la operación.
 */
@Service
public class QuotaService {

    private final MongoTemplate mongoTemplate;
    private final JdbcTemplate jdbcTemplate;

    public QuotaService(MongoTemplate mongoTemplate, JdbcTemplate jdbcTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Asegura que existe el doc en client_quotas (lazy init desde Postgres
     * Users.Max_instances) y luego hace check + increment atómico.
     * Lanza QuotaExceededException si el usuario ya alcanzó su máximo.
     */
    public void reserve(Long userId) {
        Integer maxInstances = jdbcTemplate.queryForObject(
                "SELECT \"Max_instances\" FROM \"Users\" WHERE \"User_id\" = ?",
                Integer.class, userId);

        if (maxInstances == null) {
            throw new IllegalArgumentException("Usuario no existe: " + userId);
        }

        // (1) Upsert del doc de cuota: maxInstances SIEMPRE se actualiza desde
        // Postgres (es la fuente de verdad); activeCount se inicializa solo
        // si el doc no existía. Sin esta distinción, cambios al plan del
        // usuario en Postgres no se propagan a Mongo y la cuota queda "frozen".
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(userId)),
                new Update()
                        .set("maxInstances", maxInstances)
                        .setOnInsert("activeCount", 0),
                "client_quotas");

        // (2) Check + increment atómico. El filter exige que activeCount < maxInstances.
        // BasicQuery acepta un Document BSON crudo — necesario porque Spring Data
        // Criteria no soporta $expr directamente y mutar getQueryObject() no
        // siempre se serializa bien al driver.
        org.springframework.data.mongodb.core.query.BasicQuery quotaQuery =
                new org.springframework.data.mongodb.core.query.BasicQuery(
                        new Document("_id", userId)
                                .append("$expr", new Document("$lt", List.of("$activeCount", "$maxInstances"))));
        UpdateResult res = mongoTemplate.updateFirst(
                quotaQuery,
                new Update().inc("activeCount", 1),
                "client_quotas");

        if (res.getModifiedCount() == 0) {
            throw new QuotaExceededException(
                    "El usuario ya alcanzó el máximo de instancias permitido ("
                            + maxInstances + ")");
        }
    }

    /**
     * Decrementa activeCount en 1, sólo si activeCount > 0 (idempotente).
     */
    public void release(Long userId) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(userId).and("activeCount").gt(0)),
                new Update().inc("activeCount", -1),
                "client_quotas");
    }
}