package Host_Usach_Cloud.Backend.Mongo.Repository;

import Host_Usach_Cloud.Backend.Mongo.Entity.ClientQuotaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientQuotaMongoRepository extends MongoRepository<ClientQuotaDocument, Long> {
}