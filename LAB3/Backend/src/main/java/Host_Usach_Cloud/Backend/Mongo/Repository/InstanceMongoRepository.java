package Host_Usach_Cloud.Backend.Mongo.Repository;

import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceMongoRepository extends MongoRepository<InstanceDocument, String> {

    List<InstanceDocument> findByUserId(Long userId);

    List<InstanceDocument> findByUserIdAndTerminatedFalse(Long userId);

    List<InstanceDocument> findByUserIdAndState(Long userId, String state);

    List<InstanceDocument> findByState(String state);

    List<InstanceDocument> findByUserIdAndStateIn(Long userId, List<String> states);

    long countByUserIdAndStateIn(Long userId, List<String> states);
}