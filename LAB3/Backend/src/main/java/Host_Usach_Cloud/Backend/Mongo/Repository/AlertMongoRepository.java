package Host_Usach_Cloud.Backend.Mongo.Repository;

import Host_Usach_Cloud.Backend.Mongo.Entity.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertMongoRepository extends MongoRepository<AlertDocument, String> {
    List<AlertDocument> findByUserId(Long userId);
    List<AlertDocument> findByUserIdAndRead(Long userId, boolean read);
}
